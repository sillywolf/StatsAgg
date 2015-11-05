package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.utilities.DateAndTime;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "CreateAlertSuspension", urlPatterns = {"/CreateAlertSuspension"})
public class CreateAlertSuspension extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlertSuspension.class.getName());
    
    public static final String PAGE_NAME = "Create Suspension";
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processPostRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            AlertSuspension suspension = null;
            String name = request.getParameter("Name");
            if (name != null) {
                AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
                suspension = alertSuspensionsDao.getSuspensionByName(name.trim());
            }        
            
            String htmlBodyContents = buildCreateAlertSuspensionHtml(suspension);
            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_create_alert_suspension.js");
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents, additionalJavascript);
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }

    }
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        PrintWriter out = null;
        
        try {
            String result = parseAndAlterAlertSuspension(request);
            
            response.setContentType("text/html");     
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "AlertSuspensions", AlertSuspensions.PAGE_NAME);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContent);
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }
    }
    
    private String buildCreateAlertSuspensionHtml(AlertSuspension suspension) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> \n" +
            "    <form action=\"CreateAlertSuspension\" method=\"POST\">\n");
        
        htmlBody.append("<div class=\"row create-alert-form-row\">"); 

        if ((suspension != null) && (suspension.getName() != null) && !suspension.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(Encode.forHtmlAttribute(suspension.getName())).append("\">");
        }
        
        
        // column #1
        htmlBody.append("" +
            "<div class=\"col-md-4 statsagg_three_panel_first_panel\" > \n" +
            "  <div class=\"panel panel-info\"> \n" +
            "    <div class=\"panel-heading\"><b>Options</b></div> \n" +
            "    <div class=\"panel-body\"> \n");
            
        // name
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Name</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\".\" name=\"Name\" id=\"Name\"");
        
        if ((suspension != null) && (suspension.getName() != null)) {
            htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(suspension.getName())).append("\"");
        }

        htmlBody.append(">\n" + "</div>\n");
        
        
        // description
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\">");

        if ((suspension != null) && (suspension.getDescription() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(suspension.getDescription()));
        }

        htmlBody.append("</textarea>\n");
        htmlBody.append("</div>\n");
        
        
        // is enabled?
        htmlBody.append("" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"Enabled\" id=\"Enabled\" type=\"checkbox\" ");

        if (((suspension != null) && (suspension.isEnabled() != null) && suspension.isEnabled()) || 
                (suspension == null) || (suspension.isEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
       

        // suspend notification only?
        htmlBody.append("" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Suspend notification only?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"SuspendNotificationOnly\" id=\"SuspendNotificationOnly\" type=\"checkbox\" ");

        if ((suspension != null) && (suspension.isSuspendNotificationOnly() != null) && suspension.isSuspendNotificationOnly() ||
                (suspension == null) || (suspension.isSuspendNotificationOnly() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");

        // end column 1
        htmlBody.append("</div></div></div>");

        
        
        // column #2
        htmlBody.append("" +
            "<div class=\"col-md-4 statsagg_three_panel_second_panel\" > \n" +
            "  <div class=\"panel panel-info\"> \n" +
            "    <div class=\"panel-heading\"><b>Suspend by...</b>" +
            "         <a id=\"AlertSuspensionAlertAssociationsPreview\" name=\"AlertSuspensionAlertAssociationsPreview\" class=\"iframe cboxElement statsagg_alert_suspension_alert_associations_preview pull-right\" href=\"#\" onclick=\"generateAlertSuspensionAssociationsPreviewLink();\">Preview Suspension Associations</a>" + 
            "    </div>" + 
            "    <div class=\"panel-body\"> \n");
            
        
        // type selection
        htmlBody.append("<input type=\"radio\" id=\"CreateSuspension_SuspendBy_AlertName_Radio\" name=\"CreateSuspension_SuspendBy\" value=\"AlertName\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Alert Name &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"CreateSuspension_SuspendBy_Tags_Radio\" name=\"CreateSuspension_SuspendBy\" value=\"Tags\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Tags &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"CreateSuspension_SuspendBy_Everything_Radio\" name=\"CreateSuspension_SuspendBy\" value=\"Everything\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING)) htmlBody.append(" checked=\"checked\" ");
        htmlBody.append("> Everything &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"CreateSuspension_SuspendBy_Metrics_Radio\" name=\"CreateSuspension_SuspendBy\" value=\"Metrics\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRICS)) htmlBody.append(" checked=\"checked\" ");
        htmlBody.append("> Metrics\n");
        
        htmlBody.append("<br><br>\n");
        
        
        // alert name
        htmlBody.append("" +
            "<div id=\"CreateSuspension_SuspendBy_AlertName_Div\">\n" +
            "  <div class=\"form-group\" id=\"AlertNameLookup\"> \n" +
            "    <input class=\"typeahead form-control-statsagg\" placeholder=\"Enter the name of the alert that you want to suspend.\" autocomplete=\"off\" name=\"AlertName\" id=\"AlertName\" ");
        
        if ((suspension != null) && (suspension.getAlertId() != null)) {
            AlertsDao alertsDao = new AlertsDao();
            Alert alert = alertsDao.getAlert(suspension.getAlertId());
            
            if ((alert != null) && (alert.getName() != null)) htmlBody.append("value=\"").append(Encode.forHtmlAttribute(alert.getName())).append("\"");
        }
 
        htmlBody.append("" +
            "> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric group tags (inclusive)
        htmlBody.append("" +
            "<div id=\"CreateSuspension_SuspendBy_Tags_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\"For an alert to be suspended, it must be tagged with ALL of the tags listed here. " +
            "List one tag per line.\" rows=\"5\" name=\"MetricGroupTagsInclusive\" id=\"MetricGroupTagsInclusive\" >");
        
        if ((suspension != null) && (suspension.getMetricGroupTagsInclusive() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(suspension.getMetricGroupTagsInclusive()));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric group tags (exclusive)
        htmlBody.append("" +
            "<div id=\"CreateSuspension_SuspendBy_Everything_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\"For an alert to be excluded from suspension, it must be tagged with ANY of the tags listed here. " +
            "List one tag per line.\" rows=\"5\" name=\"MetricGroupTagsExclusive\" id=\"MetricGroupTagsExclusive\" >");
        
        if ((suspension != null) && (suspension.getMetricGroupTagsExclusive() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(suspension.getMetricGroupTagsExclusive()));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric suspension
        htmlBody.append("" +
            "<div id=\"CreateSuspension_SuspendBy_Metrics_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\" " +
            "List one regex per line.\" rows=\"5\" name=\"MetricSuspensionRegexes\" id=\"MetricSuspensionRegexes\" >");
        
        if ((suspension != null) && (suspension.getMetricSuspensionRegexes() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(suspension.getMetricSuspensionRegexes()));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        // end column 2
        htmlBody.append("</div></div></div>");
        
  
        
        // column #3
        htmlBody.append("" +
            "<div class=\"col-md-4 statsagg_three_panel_third_panel\" > \n" +
            "   <div class=\"panel panel-info\"> \n" +
            "       <div class=\"panel-heading\"><b>Suspension Type</b></div> \n" +
            "       <div class=\"panel-body\"> \n");
        
        
        // one time or recurring?
        String startSuspensionTypeRecurring = "<input type=\"radio\" id=\"CreateAlertSuspension_Type_Recurring\" name=\"CreateAlertSuspension_Type\" value=\"Recurring\" ";
        String endSuspensionTypeRecurring = " > Recurring (daily)&nbsp;&nbsp;&nbsp; \n";
        String startSuspensionTypeOneTime = "<input type=\"radio\" id=\"CreateAlertSuspension_Type_OneTime\" name=\"CreateAlertSuspension_Type\" value=\"OneTime\" ";
        String endSuspensionTypeOneTime = " > One Time \n";

        if ((suspension != null) && (suspension.isOneTime() != null)) {
            if (suspension.isOneTime()) {
                htmlBody.append(startSuspensionTypeRecurring).append(endSuspensionTypeRecurring)
                        .append(startSuspensionTypeOneTime).append(" checked=\"checked\" ").append(endSuspensionTypeOneTime);
            }
            else {
                htmlBody.append(startSuspensionTypeRecurring).append(" checked=\"checked\" ").append(endSuspensionTypeRecurring)
                        .append(startSuspensionTypeOneTime).append(endSuspensionTypeOneTime);
            }
        }
        else {
                htmlBody.append(startSuspensionTypeRecurring).append(endSuspensionTypeRecurring)
                        .append(startSuspensionTypeOneTime).append(endSuspensionTypeOneTime);
        }
        
        htmlBody.append("" +
            "<br><br>\n" +
            "<table style=\"border-spacing:0px 2px; width:100%;\">\n" +
            "<tbody>\n");
        
        
        // start date
        htmlBody.append(        
            "<tr>\n" +
            "  <th style=\"width:1%;\"><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_DateTimePicker_StartDate_Label_Div\">Start Date:</div></th>\n" +
            "  <td>\n" +
            "    <div class=\"input-group\" id=\"CreateAlertSuspension_DateTimePicker_StartDate_Div\" style=\"width:100%;\"> \n" +
            "      <input class=\"form-control-datetime\" name=\"StartDate\" id=\"StartDate\" style=\"width:100%;\" type=\"text\" ");
        
        if ((suspension != null) && (suspension.getStartDate() != null)) {
            String startDateString = DateAndTime.getFormattedDateAndTime(suspension.getStartDate(), "MM/dd/yyyy");
            htmlBody.append(" value=\"").append(startDateString).append("\"");
        }
        
        htmlBody.append(       
            " > \n" +
            "      <span class=\"input-group-addon\" style=\"font-size:10px;\" ><span class=\"glyphicon-calendar glyphicon\"></span></span> \n" +
            "    </div> \n" +
            "  </td>\n" +
            "</tr>\n");
         
        
        // recur on days of week
        htmlBody.append(        
            "<tr>\n" +
            "  <th><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_RecurOnDays_Label_Div\">Recurs on:</div></th>\n" +
            "  <td>\n" +
            "    <div id=\"CreateAlertSuspension_RecurOnDays_Div\">\n");

        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurSunday\" id=\"RecurSunday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurSunday() == null) || suspension.isRecurSunday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">S</label>\n");      
                
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurMonday\" id=\"RecurMonday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurMonday() == null) || suspension.isRecurMonday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">M</label>\n");      
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurTuesday\" id=\"RecurTuesday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurTuesday() == null) || suspension.isRecurTuesday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">T</label>\n");      
     
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurWednesday\" id=\"RecurWednesday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurWednesday() == null) || suspension.isRecurWednesday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">W</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurThursday\" id=\"RecurThursday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurThursday() == null) || suspension.isRecurThursday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">T</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurFriday\" id=\"RecurFriday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurFriday() == null) || suspension.isRecurFriday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">F</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurSaturday\" id=\"RecurSaturday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurSaturday() == null) || suspension.isRecurSaturday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">S</label>\n");   
        
        htmlBody.append("</div>\n" + "</td>\n" + "</tr>\n");
        
        htmlBody.append("<tr id=\"CreateAlertSuspension_Type_Spacer1\"><th>&nbsp;</th><td>&nbsp;</td></tr>\n");
        
        
        // start time
        htmlBody.append(
            "<tr>\n" +
            "  <th><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_DateTimePicker_StartTime_Label_Div\">Start Time:</div></th>\n" +
            "    <td>\n" +
            "      <div class=\"input-group\" id=\"CreateAlertSuspension_DateTimePicker_StartTime_Div\" style=\"width:100%;\" > \n" +
            "        <input class=\"form-control-datetime\" name=\"StartTime\" id=\"StartTime\" style=\"width:100%;\" type=\"text\" ");
        
        if ((suspension != null) && (suspension.getStartTime() != null)) {
            String startTimeString = DateAndTime.getFormattedDateAndTime(suspension.getStartTime(), "h:mm a");
            htmlBody.append(" value=\"").append(startTimeString).append("\"");
        }
  
        htmlBody.append("" +
            "> \n" +
            "        <span class=\"input-group-addon\" style=\"font-size:10px;\"><span class=\"glyphicon-time glyphicon\"></span></span> \n" +
            "      </div>\n" +
            "    </td>\n" +
            "</tr>\n");
        
        
        // duration
        htmlBody.append(
            "<tr>\n" +
            "  <th><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_Duration_Label_Div\">Duration:</div></th>\n" +
            "  <td>\n" +
            "    <div style=\" padding-top: 3px;\" id=\"CreateAlertSuspension_Duration_Div\">\n" +
            "      <div class=\"col-xs-6\"> <input class=\"form-control-statsagg\" name=\"Duration\" id=\"Duration\" ");
                    
        if ((suspension != null) && (suspension.getDuration() != null)) {
            BigDecimal duration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(suspension.getDuration(), suspension.getDurationTimeUnit());
            htmlBody.append(" value=\"").append(duration.stripTrailingZeros().toPlainString()).append("\"");
        }
        htmlBody.append("></div>\n");

        
        // duration time unit
        htmlBody.append("<div class=\"col-xs-6\"> <select class=\"form-control-statsagg col-xs-6\" name=\"DurationTimeUnit\" id=\"DurationTimeUnit\">\n");

        if ((suspension != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(suspension.getDurationTimeUnit(), true) != null)) {
            String whiteSpace = "							  ";
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(suspension.getDurationTimeUnit(), true);

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select></div>\n");
        htmlBody.append("</div>\n </td>\n </tr>\n");

        // end column 3
        htmlBody.append("</tbody>\n </table>\n </div>\n </div>\n </div>\n");
        
      

        // end form & body content
        htmlBody.append(        
            "</div>" +
            "<button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>\n" +
            "&nbsp;&nbsp;&nbsp;" +
            "<a href=\"AlertSuspensions\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>");
        
        htmlBody.append("</form></div></div>");
        
        return htmlBody.toString();
    }
    
    public String parseAndAlterAlertSuspension(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        AlertSuspension suspension = getSuspensionFromRequestParameters(request);
        String oldName = Common.getObjectParameter(request, "Old_Name");
        
        // insert/update/delete records in the database
        if (suspension != null) {
            AlertSuspensionsLogic suspensionsLogic = new AlertSuspensionsLogic();
            returnString = suspensionsLogic.alterRecordInDatabase(suspension, oldName);
            
            if (suspensionsLogic.getLastAlterRecordStatus() == AlertSuspensionsLogic.STATUS_CODE_SUCCESS) {
                logger.info("Running suspension routine");
                com.pearson.statsagg.alerts.AlertSuspensions suspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
                suspensions.runAlertSuspensionRoutine();
            }
        }
        else {
            returnString = "Failed to add suspension. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    private AlertSuspension getSuspensionFromRequestParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        AlertSuspension suspension = new AlertSuspension();

        try {
            String parameter;

            // column #1 parameters
            parameter = Common.getObjectParameter(request, "Name");
            String trimmedName = parameter.trim();
            suspension.setName(trimmedName);
            suspension.setUppercaseName(trimmedName.toUpperCase());
            if ((suspension.getName() == null) || suspension.getName().isEmpty()) didEncounterError = true;
            
            parameter = Common.getObjectParameter(request, "Description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                suspension.setDescription(description);
            }
            else suspension.setDescription("");
            
            parameter = Common.getObjectParameter(request, "Enabled");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsEnabled(true);
            else suspension.setIsEnabled(false);

            parameter = Common.getObjectParameter(request, "SuspendNotificationOnly");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsSuspendNotificationOnly(true);
            else suspension.setIsSuspendNotificationOnly(false);
 
            
            // column #2 parameters
            parameter = Common.getObjectParameter(request, "CreateSuspension_SuspendBy");
            if ((parameter != null) && parameter.contains("AlertName")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_ALERT_ID);
            else if ((parameter != null) && parameter.contains("Tags")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS);
            else if ((parameter != null) && parameter.contains("Everything")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_EVERYTHING);
            else if ((parameter != null) && parameter.contains("Metrics")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_METRICS);

            parameter = Common.getObjectParameter(request, "AlertName");
            AlertsDao alertsDao = new AlertsDao();
            Alert alert = alertsDao.getAlertByName(parameter);
            if (alert != null) suspension.setAlertId(alert.getId());

            parameter = Common.getObjectParameter(request, "MetricGroupTagsInclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsInclusive(trimmedTags);
            }
            
            parameter = Common.getObjectParameter(request, "MetricGroupTagsExclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsExclusive(trimmedTags);
            }
            
            parameter = Common.getObjectParameter(request, "MetricSuspensionRegexes");
            if (parameter != null) {
                String metricSuspensionRegexes = AlertSuspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricSuspensionRegexes(metricSuspensionRegexes);
            }
            
            
            // column #3 parameters
            parameter = Common.getObjectParameter(request, "CreateAlertSuspension_Type");
            if ((parameter != null) && parameter.contains("Recurring")) suspension.setIsOneTime(false);
            else if ((parameter != null) && parameter.contains("OneTime")) suspension.setIsOneTime(true);
            
            parameter = Common.getObjectParameter(request, "StartDate");
            if (parameter != null) {
                String startDateStringTrimmed = parameter.trim();
                Calendar startDateCalendar = DateAndTime.getCalendarFromFormattedString(startDateStringTrimmed, "MM/dd/yyyy");
                Timestamp startDateTimestamp = new Timestamp(startDateCalendar.getTimeInMillis());
                suspension.setStartDate(startDateTimestamp);
            }
            
            parameter = Common.getObjectParameter(request, "RecurSunday");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsRecurSunday(true);
            else suspension.setIsRecurSunday(false);
            
            parameter = Common.getObjectParameter(request, "RecurMonday");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsRecurMonday(true);
            else suspension.setIsRecurMonday(false);
            
            parameter = Common.getObjectParameter(request, "RecurTuesday");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsRecurTuesday(true);
            else suspension.setIsRecurTuesday(false);
            
            parameter = Common.getObjectParameter(request, "RecurWednesday");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsRecurWednesday(true);
            else suspension.setIsRecurWednesday(false);
            
            parameter = Common.getObjectParameter(request, "RecurThursday");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsRecurThursday(true);
            else suspension.setIsRecurThursday(false);

            parameter = Common.getObjectParameter(request, "RecurFriday");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsRecurFriday(true);
            else suspension.setIsRecurFriday(false);
            
            parameter = Common.getObjectParameter(request, "RecurSaturday");
            if ((parameter != null) && parameter.contains("on")) suspension.setIsRecurSaturday(true);
            else suspension.setIsRecurSaturday(false);

            parameter = Common.getObjectParameter(request, "StartTime");
            if (parameter != null) {
                String startTimeStringTrimmed = parameter.trim();
                String startTimeStringTrimmedWithDate = "01/01/1970" + " " + startTimeStringTrimmed;
                Calendar startTimeCalendar = DateAndTime.getCalendarFromFormattedString(startTimeStringTrimmedWithDate, "MM/dd/yyyy h:mm a");
                Timestamp startTimeTimestamp = new Timestamp(startTimeCalendar.getTimeInMillis());
                suspension.setStartTime(startTimeTimestamp);
            }
            
            parameter = Common.getObjectParameter(request, "DurationTimeUnit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    suspension.setDurationTimeUnit(intValue);
                }
            }
            
            parameter = Common.getObjectParameter(request, "Duration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, suspension.getDurationTimeUnit());
                    if (timeInMs != null) suspension.setDuration(timeInMs.longValue());
                }
            }

            if ((suspension.isOneTime() != null) && suspension.isOneTime() && (suspension.getDuration() != null) &&
                    (suspension.getStartDate() != null) && (suspension.getStartTime() != null)) {
                Calendar startTime = Calendar.getInstance();
                startTime.setTimeInMillis(suspension.getStartTime().getTime());
               
                Calendar startDateAndTime = Calendar.getInstance();
                startDateAndTime.setTimeInMillis(suspension.getStartDate().getTime());
                startDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(startDateAndTime, 
                        startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), startTime.get(Calendar.SECOND), startTime.get(Calendar.MILLISECOND));
                
                Calendar deleteDateAndTime = Calendar.getInstance();
                deleteDateAndTime.setTimeInMillis(startDateAndTime.getTimeInMillis());
                deleteDateAndTime.add(Calendar.SECOND, (int) (suspension.getDuration() / 1000));
                
                Timestamp deleteAtTimestamp = new Timestamp(deleteDateAndTime.getTimeInMillis());
                suspension.setDeleteAtTimestamp(deleteAtTimestamp);
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) suspension = null;
        boolean isValid = AlertSuspension.isValid(suspension);
        if (!isValid) suspension = null;
        
        return suspension;
    }
    
}
