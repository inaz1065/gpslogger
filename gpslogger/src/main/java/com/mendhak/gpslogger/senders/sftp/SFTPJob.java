package com.mendhak.gpslogger.senders.sftp;


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

public class SFTPJob extends Job {
    private static final Logger LOG = Logs.of(SFTPJob.class);
    private final File localFile;
    private final SFTPSettings settings;

    public SFTPJob(File localFile, SFTPSettings settings){
        super(new Params(1).requireNetwork().persist().addTags(getJobTag(localFile)));
        this.localFile = localFile;
        this.settings = settings;
    }

    @Override
    public void onAdded() {
        LOG.debug("SFTP Job added");
    }

    @Override
    public void onRun() throws Throwable {
        LOG.debug("SFTP Job onRun");
        com.jcraft.jsch.Session session = null;
        final JSch jsch = new JSch();
        FileInputStream fis = null;

        try {
            String keystring = this.settings.getKnownHostKey();

            if (!Strings.isNullOrEmpty(keystring)) {
                byte[] key = Base64.decode(keystring, Base64.DEFAULT);
                jsch.getHostKeyRepository().add(new HostKey(settings.getHost(), key), null);
            }

            if(!Strings.isNullOrEmpty(this.settings.getPrivateKeyFilePath())){
                jsch.addIdentity(this.settings.getPrivateKeyFilePath(), this.settings.getPrivateKeyPassphrase());
            }

            session = jsch.getSession(this.settings.getUser(), this.settings.getHost(), this.settings.getPort());
            session.setPassword(this.settings.getPassword());

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "yes");
            session.setConfig(prop);

            LOG.debug("Connecting...");
            session.connect();

            if (session.isConnected()) {

                LOG.debug("Connected, opening SFTP channel");
                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp channelSftp = (ChannelSftp) channel;
                LOG.debug("Changing directory to " + this.settings.getRemoteServerPath());
                channelSftp.cd(this.settings.getRemoteServerPath());
                LOG.debug("Uploading " + this.localFile.getName() + " to remote server");
                channelSftp.put(new FileInputStream(this.localFile), this.localFile.getName(), ChannelSftp.OVERWRITE);

                LOG.debug("Disconnecting");
                channelSftp.disconnect();
                channel.disconnect();
                session.disconnect();

                EventBus.getDefault().post(new UploadEvents.SFTP().succeeded());
            } else {
                EventBus.getDefault().post(new UploadEvents.SFTP().failed("Could not connect, unknown reasons", null));
            }

        } catch (SftpException sftpex) {
            LOG.error(sftpex.getMessage(), sftpex);
            EventBus.getDefault().post(new UploadEvents.SFTP().failed(sftpex.getMessage(), sftpex));
        } catch (final JSchException jex) {
            LOG.error(jex.getMessage(), jex);
            if (jex.getMessage().contains("reject HostKey") || jex.getMessage().contains("HostKey has been changed")) {
                LOG.debug(session.getHostKey().getKey());
                UploadEvents.SFTP sftpException = new UploadEvents.SFTP();
                sftpException.hostKey = session.getHostKey().getKey();
                sftpException.fingerprint = session.getHostKey().getFingerPrint(jsch);
                EventBus.getDefault().post(sftpException.failed(jex.getMessage(), jex));
            } else {
                throw jex;
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            EventBus.getDefault().post(new UploadEvents.SFTP().failed(ex.getMessage(), ex));
        } finally {
            try {
                fis.close();
            } catch (Exception ee) {
            }
        }
    }

    @Override
    protected void onCancel() {

        LOG.debug("SFTP Job Cancelled");
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        LOG.error("Could not upload to SFTP server", throwable);
        EventBus.getDefault().post(new UploadEvents.SFTP().failed(throwable.getMessage(), throwable));
        return false;
    }

    public static String getJobTag(File gpxFile) {
        return "SFTP" + gpxFile.getName();
    }
}
