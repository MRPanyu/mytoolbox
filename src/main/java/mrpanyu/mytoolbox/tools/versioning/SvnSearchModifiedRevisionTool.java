package mrpanyu.mytoolbox.tools.versioning;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.api.UserInterface;

public class SvnSearchModifiedRevisionTool extends Tool {

	@Override
	public void initialize() {
		setName("0111_svnSearch");
		setDisplayName("SVN：根据修改内容查找版本");
		setDescription("从某个SVN文件各版本修改记录中搜索某行文本内容，打印符合的版本信息。");
		setEnableProfile(true);
		// 参数
		Parameter param = new Parameter("fileUrl", "文件URL");
		param.setDescription("具体文件的SVN URL路径，例如<i>https://host:port/svn/repo1/project1/src/sample.txt</i>");
		addParameter(param);
		param = new Parameter("userName", "用户名");
		param.setDescription("SVN登录用户名");
		addParameter(param);
		param = new Parameter("password", "密码");
		param.setDescription("SVN登录密码");
		addParameter(param);
		param = new Parameter("encoding", "文件编码");
		param.setDescription("文件的中午编码");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("UTF-8", "GBK"));
		addParameter(param);
		param = new Parameter("searchContent", "修改内容");
		param.setDescription("搜索的修改内容，必须在单行内，可以只包含部分特征性文本");
		addParameter(param);
		// 操作
		addAction(new Action("search", "查找"));
	}

	@Override
	public void performAction(String actionName) {
		UserInterface ui = getUserInterface();
		String fileUrl = getParameter("fileUrl").getValue();
		String userName = getParameter("userName").getValue();
		String password = getParameter("password").getValue();
		String encoding = getParameter("encoding").getValue();
		String searchContent = getParameter("searchContent").getValue();

		ui.clearMessages();
		if (StringUtils.isBlank(fileUrl)) {
			ui.writeErrorMessage("请输入文件URL");
			return;
		}
		if (StringUtils.isBlank(userName)) {
			ui.writeErrorMessage("请输入用户名");
			return;
		}
		if (StringUtils.isBlank(password)) {
			ui.writeErrorMessage("请输入密码");
			return;
		}
		if (StringUtils.isBlank(searchContent)) {
			ui.writeErrorMessage("请输入修改内容");
			return;
		}
		try {
			searchModifiedRevision(fileUrl, userName, password, encoding, searchContent, ui);
		} catch (Exception e) {
			e.printStackTrace();
			ui.writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

	@SuppressWarnings("unchecked")
	private void searchModifiedRevision(String fileUrl, String userName, String password, String encoding,
			String searchContent, UserInterface ui) throws Exception {
		SVNRepository repo = null;
		SVNClientManager clientManager = null;
		try {
			String dirUrl = StringUtils.substringBeforeLast(fileUrl, "/");
			String fileName = StringUtils.substringAfterLast(fileUrl, "/");
			repo = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(dirUrl));
			repo.setAuthenticationManager(
					SVNWCUtil.createDefaultAuthenticationManager(userName, password.toCharArray()));
			Collection<SVNLogEntry> logs = repo.log(new String[] { fileName }, null, 0, repo.getLatestRevision(), true,
					true);
			clientManager = SVNClientManager.newInstance(new DefaultSVNOptions(), userName, password);
			SVNDiffClient diffClient = clientManager.getDiffClient();
			SVNURL svnFileUrl = SVNURL.parseURIEncoded(fileUrl);
			for (SVNLogEntry log : logs) {
				boolean foundSearchContent = searchLogEntry(log, diffClient, svnFileUrl, encoding, searchContent);
				if (foundSearchContent) {
					ui.writeInfoMessage("==== Rev " + log.getRevision() + " ====");
					ui.writeInfoMessage("Author: " + log.getAuthor());
					ui.writeInfoMessage("Date: " + DateFormatUtils.format(log.getDate(), "yyyy-MM-dd HH:mm:ss"));
					ui.writeInfoMessage("Message: " + log.getMessage());
				}
			}
			ui.writeInfoMessage("==== 查找结束 ====");
		} finally {
			if (clientManager != null) {
				clientManager.dispose();
			}
			if (repo != null) {
				repo.closeSession();
			}
		}
	}

	private boolean searchLogEntry(SVNLogEntry log, SVNDiffClient diffClient, SVNURL svnFileUrl, String encoding,
			String searchContent) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		diffClient.doDiff(svnFileUrl, SVNRevision.create(log.getRevision() - 1), svnFileUrl,
				SVNRevision.create(log.getRevision()), SVNDepth.EMPTY, true, baos);
		String diffText = new String(baos.toByteArray(), encoding);
		String[] lines = diffText.split("\n");
		boolean foundSearchContent = false;
		boolean isBody = false;
		for (String line : lines) {
			if (line.startsWith("@@")) {
				isBody = true;
			} else if (isBody && (line.startsWith("+") || line.startsWith("-"))) {
				String lineText = line.substring(1);
				if (lineText.contains(searchContent)) {
					foundSearchContent = true;
					break;
				}
			}

		}
		return foundSearchContent;
	}

}
