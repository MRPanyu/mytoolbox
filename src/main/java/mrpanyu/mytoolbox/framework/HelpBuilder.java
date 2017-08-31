package mrpanyu.mytoolbox.framework;

import org.apache.commons.lang.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.Tool;

public class HelpBuilder {

	public static String buildHelp(Tool tool) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><style type='text/css'>");
		sb.append(".toolname {font-size:20px;font-weight:bold;color:#0000FF}");
		sb.append(".tooldesc {font-size:14px;color:#000000}");
		sb.append(".paramstitle, .actionstitle {font-size:18px;font-weight:bold;color:#00CC00;margin-top:20px;}");
		sb.append(".paramslist, .actionslist {font-size:13px;}");
		sb.append(".paramname, .actionname {font-weight:bold;color:#0000FF}");
		sb.append("</style></head><body>");
		sb.append("<div class='toolname'>").append(tool.getDisplayName()).append("</div>");
		sb.append("<div class='tooldesc'>").append(StringUtils.trimToEmpty(tool.getDescription())).append("</div>");
		sb.append("<div class='paramstitle'>参数说明：</div>");
		sb.append("<ul class='paramslist'>");
		for (Parameter param : tool.getParameters()) {
			sb.append("<li>");
			sb.append("<span class='paramname'>").append(param.getDisplayName()).append("</span>");
			sb.append(" - ");
			sb.append("<span class='paramdesc'>").append(StringUtils.trimToEmpty(param.getDescription()))
					.append("</span>");
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<div class='actionstitle'>操作说明：</div>");
		sb.append("<ul class='actionslist'>");
		for (Action a : tool.getActions()) {
			sb.append("<li>");
			sb.append("<span class='actionname'>").append(a.getDisplayName()).append("</span>");
			sb.append(" - ");
			sb.append("<span class='actiondesc'>").append(StringUtils.trimToEmpty(a.getDescription()))
					.append("</span>");
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("</body>");
		return sb.toString();
	}

}
