package org.github.rongqin.ssh;

import cn.hutool.core.lang.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.DefaultScpClientCreator;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.github.rongqin.model.CommandResponse;
import org.github.rongqin.util.AssertUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * ssh client for sshd
 * @author herongqin
 */
@Slf4j
public final class SshdClient {

	private ClientSession session;

	private SshClient sshClient;

	@Getter
	private SshConfig sshConfig;



	public SshdClient(SshConfig sshConfig){
		this.sshConfig = sshConfig;

	}

	private void openSession(){
		if (this.session == null){
			this.sshClient = SshClient.setUpDefaultClient();
			// 设置密码
			if (sshConfig.getPrivateKeyPath() != null && !sshConfig.getPrivateKeyPath().equals("")){
				// TODO： KeyPair 加载
			}
			this.sshClient.addPasswordIdentity(sshConfig.getPassword());
			sshClient.start();
			try {
				// 通过主机IP、user、port 获取ssh session
				this.session = sshClient.connect(sshConfig.getUsername(), sshConfig.getHost(), sshConfig.getPort()).verify().getSession();
				this.session.addPasswordIdentity(sshConfig.getPassword());
				Assert.isTrue(this.session.auth().verify(10, TimeUnit.SECONDS).isSuccess(), "ssh ["+sshConfig.getHost()+"] 连接失败。");
			} catch (Exception e) {
				log.error("ssh client open session error: {}", e);
			}
		}
	}

	public void close() throws Exception {
		if (session != null){
			session.close();
		}
		if (sshClient != null){
			sshClient.stop();
			sshClient.close();
		}
	}

	/**
	 * 上传文件
	 * @param localFilePath 本地文件目录
	 * @param remotePath
	 * @return
	 */
	public void scpPutFile(String localFilePath, String remotePath){
		ScpClient scpClient = null;

		try {
			openSession();
			ScpClientCreator scpClientCreator = new DefaultScpClientCreator();
			// 创建scp 客户端
			scpClient = scpClientCreator.createScpClient(session);
			scpClient.upload(localFilePath, remotePath, ScpClient.Option.Recursive);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			// 释放
			scpClient = null;
		}
	}

	/**
	 * 文件下载
	 * @param localPath
	 * @param remoteFilePath
	 */
	public void scpDownload(String localPath, String remoteFilePath){
		ScpClient scpClient = null;

		try {
			ScpClientCreator scpClientCreator = new DefaultScpClientCreator();
			// 创建scp 客户端
			scpClient = scpClientCreator.createScpClient(session);
			scpClient.download(remoteFilePath, localPath, ScpClient.Option.Recursive);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			// 释放
			scpClient = null;
		}
	}

	/**
	 * 执行命令
	 * @param command 命令
	 * @return
	 */
	public CommandResponse execCommand(String command){

		ChannelExec channelExec = null;
		try {
			openSession();
			channelExec = session.createExecChannel(command);
			int time = 0;
			boolean run = false;
			channelExec.open();
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BufferedReader reader = null;
			BufferedReader error_reader = null;
			channelExec.setOut(out);
			channelExec.setErr(err);
			while (true){
				if (channelExec.isClosed() || run){
					break;
				}
				Thread.sleep(100);
				if (time > 180){
					break;
				}
				time ++;
			}

			return CommandResponse.builder()
					.state(channelExec.getExitStatus())
					.info(out.toString())
					.error(err.toString())
					.build();

		}catch (Exception e){
			log.error("exec command error: {}", e);
			return CommandResponse.builder()
					.state(1)
					.error(e.getMessage())
					.build();
		}finally {
			if (channelExec != null){
				try {
					channelExec.close();
				} catch (IOException e) {
					log.error("close channelExec error : {}", e.getMessage());
				}
			}
		}
	}

	private Collection<KeyPair> loadKeyPairs(String filePath, String keyName, String publicKey){

		return null;
	}



}
