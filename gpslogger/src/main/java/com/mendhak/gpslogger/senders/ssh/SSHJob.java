package com.mendhak.gpslogger.senders.ssh;


import android.util.Base64;
import com.jcraft.jsch.*;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.events.UploadEvents;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import de.greenrobot.event.EventBus;
import org.slf4j.Logger;
import java.io.*;
import java.util.Properties;

public class SSHJob extends Job {
    private static final Logger LOG = Logs.of(SSHJob.class);
    private final File localFile;
    private final String host;
    private final int port;
    private final String pathToPrivateKey;
    private final String privateKeyPassphrase;
    private final String username;
    private final String password;
    private final String hostKey;
    private final String remoteDir;

    public SSHJob(File localFile, String remoteDir, String host, int port, String pathToPrivateKey, String privateKeyPassphrase, String username, String password, String hostKey) {
        super(new Params(1).requireNetwork().persist().addTags(getJobTag(localFile)));
        this.localFile = localFile;
        this.remoteDir = remoteDir;
        this.host = host;
        this.port = port;
        this.pathToPrivateKey = pathToPrivateKey;
        this.privateKeyPassphrase = privateKeyPassphrase;
        this.username = username;
        this.password = password;
        this.hostKey = hostKey;
    }


    @Override
    public void onAdded() {
        LOG.debug("SSH Job added");
    }

    @Override
    public void onRun() throws Throwable {
        LOG.debug("SSH Job onRun");
        com.jcraft.jsch.Session session = null;
        final JSch jsch = new JSch();
        FileInputStream fis = null;

        try {
            String keystring = this.hostKey;

            if (!Strings.isNullOrEmpty(keystring)) {
                byte[] key = Base64.decode(keystring, Base64.DEFAULT);
                jsch.getHostKeyRepository().add(new HostKey(host, key), null);
            }

            jsch.addIdentity(this.pathToPrivateKey, this.privateKeyPassphrase);
            session = jsch.getSession(this.username, this.host, this.port);
            session.setPassword(this.password);

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "yes");
            session.setConfig(prop);
            session.connect();

            if (session.isConnected()) {

                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp channelSftp = (ChannelSftp) channel;
                channelSftp.cd(this.remoteDir);
                channelSftp.put(new FileInputStream(this.localFile), this.localFile.getName(), ChannelSftp.OVERWRITE);

                channelSftp.disconnect();
                channel.disconnect();
                session.disconnect();

                EventBus.getDefault().post(new UploadEvents.SSH().succeeded());
            } else {
                EventBus.getDefault().post(new UploadEvents.SSH().failed("Could not connect, unknown reasons", null));
            }

        } catch (SftpException sftpex) {
            LOG.error(sftpex.getMessage(), sftpex);
            EventBus.getDefault().post(new UploadEvents.SSH().failed(sftpex.getMessage(), sftpex));
        } catch (final JSchException jex) {

            LOG.error("", jex);
            if (jex.getMessage().contains("reject HostKey") || jex.getMessage().contains("HostKey has been changed")) {
                LOG.debug(session.getHostKey().getKey());
                UploadEvents.SSH sshException = new UploadEvents.SSH();
                sshException.hostKey = session.getHostKey().getKey();
                sshException.fingerprint = session.getHostKey().getFingerPrint(jsch);
                EventBus.getDefault().post(sshException.failed(jex.getMessage(), jex));
            } else {
                throw jex;
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            EventBus.getDefault().post(new UploadEvents.SSH().failed(ex.getMessage(), ex));
        } finally {
            try {
                fis.close();
            } catch (Exception ee) {
            }
        }
    }


    @Override
    protected void onCancel() {

        LOG.debug("SSH Job Cancelled");
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        LOG.error("Could not upload to SSH server", throwable);
        EventBus.getDefault().post(new UploadEvents.SSH().failed(throwable.getMessage(), throwable));
        return false;
    }

    public static String getJobTag(File gpxFile) {
        return "SSH" + gpxFile.getName();
    }
}
