package org.ukiuni.monitorremotejobplugin.monitorremotejob;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ParametersAction;
import hudson.model.PasswordParameterValue;
import hudson.model.StringParameterValue;
import hudson.model.TextParameterValue;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.ukiuni.callOtherJenkins.CallOtherJenkins.JenkinsRemoteIF;
import org.ukiuni.callOtherJenkins.CallOtherJenkins.JenkinsRemoteIF.LastCompleteBuild;
import org.ukiuni.callOtherJenkins.CallOtherJenkins.util.ReplaceUtil;
import org.ukiuni.callOtherJenkins.CallOtherJenkins.util.TimeParser;

/**
 * Monitor remote job. {@link Builder}.
 * 
 * 
 * @author ukiuni
 */
public class MonitorRemoteJobBuilder extends Builder {

	private final String hostName;
	private final String jobName;
	private final String timeBefore;
	private final String userName;
	private final String password;
	private final boolean useSSL;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public MonitorRemoteJobBuilder(String hostName, String jobName, String timeBefore, String userName, String password, boolean useSSL) {
		this.hostName = hostName;
		this.jobName = jobName;
		this.timeBefore = timeBefore;
		this.userName = userName;
		this.password = password;
		this.useSSL = useSSL;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getHostName() {
		return hostName;
	}

	public String getJobName() {
		return jobName;
	}

	public String getTimeBefore() {
		return timeBefore;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public boolean getUseSSL() {
		return useSSL;
	}

	@Override
	public boolean perform(@SuppressWarnings("rawtypes") AbstractBuild build, Launcher launcher, BuildListener listener) {
		try {
			ParametersAction parameters = build.getAction(ParametersAction.class);
			Map<String, String> parameterMap = new HashMap<String, String>();
			if (null != parameters) {
				for (ParameterValue parameterValue : parameters.getParameters()) {
					if (parameterValue instanceof TextParameterValue) {
						parameterMap.put(parameterValue.getName(), ((TextParameterValue) parameterValue).value);
					}
					if (parameterValue instanceof StringParameterValue) {
						parameterMap.put(parameterValue.getName(), ((StringParameterValue) parameterValue).value);
					}
					if (parameterValue instanceof PasswordParameterValue) {
						parameterMap.put(parameterValue.getName(), ((PasswordParameterValue) parameterValue).getValue().getPlainText());
					}
				}
			}
			JenkinsRemoteIF jenkinsRemoteIF = new JenkinsRemoteIF(getHostName(), getJobName(), getUseSSL());
			if (null != getUserName() && !"".equals(getUserName())) {
				jenkinsRemoteIF.setAuthentication(ReplaceUtil.replaceParam(getUserName(), parameterMap), ReplaceUtil.replaceParam(getPassword(), parameterMap));
			}
			LastCompleteBuild lastCompleteBuild = jenkinsRemoteIF.loadLastCompleteBuild(listener.getLogger());
			listener.getLogger().println("build number " + lastCompleteBuild.number + " :success ? " + lastCompleteBuild.success);
			Date since = new Date(new Date().getTime() - TimeParser.parse(this.timeBefore));
			return since.before(lastCompleteBuild.date);
		} catch (Exception e) {
			e.printStackTrace(listener.getLogger());
			return false;
		}
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link CallOtherJenkinsBuilder}. Used as a singleton. The
	 * class is marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See
	 * <tt>src/main/resources/org/ukiuni/callOtherJenkins/CallOtherJenkinsBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 * 
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 * 
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 */
		public FormValidation doCheckTimeBefore(@QueryParameter String value) throws IOException, ServletException {
			try {
				TimeParser.parse(value);
				return FormValidation.ok();
			} catch (NumberFormatException e) {
				return FormValidation.error("Please enter number+kind like \"1d\", like \"1m\"");
			}
			// return FormValidation.warning("Isn't the name too short?");
		}

		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Monitor remote jenkins job";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			// https = formData.getBoolean("https");
			// ^Can also use req.bindJSON(this, formData);
			// (easier when there are many fields; need set* methods for this,
			// like setUseFrench)
			save();
			return super.configure(req, formData);
		}
	}

}
