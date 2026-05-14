package com.company.lms.service;

import com.company.lms.model.Employee;
import com.company.lms.model.LeaveRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import jakarta.activation.DataHandler;
import jakarta.faces.context.FacesContext;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

@ApplicationScoped
public class EmailService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String SMTP_HOST = getConfig("LMS_MAIL_HOST", "lms.mail.host", "smtp.gmail.com");
    private static final String SMTP_PORT = getConfig("LMS_MAIL_PORT", "lms.mail.port", "587");
    private static final String SMTP_USERNAME = getConfig("LMS_MAIL_USERNAME", "lms.mail.username", "");
    private static final String SMTP_PASSWORD = getConfig("LMS_MAIL_PASSWORD", "lms.mail.password", "");
    private static final String FROM_EMAIL = getConfig("LMS_MAIL_FROM", "lms.mail.from", SMTP_USERNAME);
    private static final String MAIL_ENABLED = getConfig("LMS_MAIL_ENABLED", "lms.mail.enabled", "false");
    private static final String APP_BASE_URL = getConfig("LMS_APP_BASE_URL", "lms.app.base.url", "http://localhost:8080/leave-management-system");

    public void sendWelcomeEmail(Employee employee) {
        if (employee == null || employee.getEmail() == null) {
            return;
        }

        String subject = "Καλωσήρθατε στο Σύστημα Διαχείρισης Αδειών";

        String body = renderTemplate("welcome.html", Map.of(
                "employeeName", getFullName(employee)
        ));

        sendEmailSafely(employee.getEmail(), subject, body);
    }

    public void sendLeaveSubmittedEmailToManager(Employee employee, LeaveRequest request) {
        if (employee == null || request == null || employee.getManager() == null) {
            return;
        }

        Employee manager = employee.getManager();

        if (manager.getEmail() == null) {
            return;
        }

        String subject = "Νέο αίτημα άδειας προς έγκριση";

        String body = renderTemplate("leave-submitted-manager.html", Map.of(
                "managerName", getFullName(manager),
                "employeeName", getFullName(employee),
                "leaveType", safe(request.getLeaveType()),
                "startDate", formatDate(request.getStartDate()),
                "endDate", formatDate(request.getEndDate()),
                "workingDays", getWorkingDaysText(request),
                "reason", safe(request.getReason()),
                "requestUrl", APP_BASE_URL + "/manager/requests.xhtml"
        ));

        sendEmailSafely(manager.getEmail(), subject, body);
    }

    public void sendLeaveApprovedEmailToEmployee(Employee employee, LeaveRequest request) {
        if (employee == null || request == null || employee.getEmail() == null) {
            return;
        }

        String subject = "Έγκριση αιτήματος άδειας";

        String body = renderTemplate("leave-approved.html", Map.of(
                "employeeName", getFullName(employee),
                "leaveType", safe(request.getLeaveType()),
                "startDate", formatDate(request.getStartDate()),
                "endDate", formatDate(request.getEndDate()),
                "workingDays", getWorkingDaysText(request),
                "managerComment", safe(request.getManagerComment()),
                "requestUrl", APP_BASE_URL + "/employee/history.xhtml"
        ));

        sendEmailSafely(employee.getEmail(), subject, body);
    }

    public void sendLeaveRejectedEmailToEmployee(Employee employee, LeaveRequest request) {
        if (employee == null || request == null || employee.getEmail() == null) {
            return;
        }

        String subject = "Απόρριψη αιτήματος άδειας";

        String body = renderTemplate("leave-rejected.html", Map.of(
                "employeeName", getFullName(employee),
                "leaveType", safe(request.getLeaveType()),
                "startDate", formatDate(request.getStartDate()),
                "endDate", formatDate(request.getEndDate()),
                "workingDays", getWorkingDaysText(request),
                "managerComment", safe(request.getManagerComment()),
                "requestUrl", APP_BASE_URL + "/employee/history.xhtml"
        ));

        sendEmailSafely(employee.getEmail(), subject, body);
    }

    private void sendEmailSafely(String to, String subject, String body) {
        try {
            sendEmail(to, subject, body);
        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        if (!Boolean.parseBoolean(MAIL_ENABLED)) {
            System.out.println("Email disabled. Would send to: " + to + " | Subject: " + subject);
            return;
        }

        if (SMTP_USERNAME.isBlank() || SMTP_PASSWORD.isBlank()) {
            System.err.println("Email credentials are missing. Email was not sent.");
            return;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");

        Multipart multipart = new MimeMultipart("related");

        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(body, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        byte[] logoBytes = loadWebAppResourceBytes("/resources/images/gsis_logo.png");

        MimeBodyPart imagePart = new MimeBodyPart();
        ByteArrayDataSource dataSource = new ByteArrayDataSource(logoBytes, "image/png");
        imagePart.setDataHandler(new DataHandler(dataSource));
        imagePart.setHeader("Content-ID", "<gsisLogo>");
        imagePart.setDisposition(MimeBodyPart.INLINE);

        multipart.addBodyPart(imagePart);

        message.setContent(multipart);

        Transport.send(message);
    }

    private static String getConfig(String envName, String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);

        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(envName);

        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }

    private String getFullName(Employee employee) {
        if (employee == null) {
            return "";
        }

        String firstName = employee.getFirstName() != null ? employee.getFirstName() : "";
        String lastName = employee.getLastName() != null ? employee.getLastName() : "";

        return (firstName + " " + lastName).trim();
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) {
            return "";
        }

        return date.format(DATE_FORMATTER);
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }

    private String loadTemplate(String templateName) {
        String path = "email-templates/" + templateName;

        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path)) {

            if (inputStream == null) {
                throw new IllegalStateException("Email template not found: " + path);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new IllegalStateException("Could not load email template: " + path, e);
        }
    }

    private byte[] loadWebAppResourceBytes(String path) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext == null) {
            throw new IllegalStateException("FacesContext is not available. Cannot load webapp resource: " + path);
        }

        try (InputStream inputStream = facesContext
                .getExternalContext()
                .getResourceAsStream(path)) {

            if (inputStream == null) {
                throw new IllegalStateException("Webapp resource not found: " + path);
            }

            return inputStream.readAllBytes();

        } catch (IOException e) {
            throw new IllegalStateException("Could not load webapp resource: " + path, e);
        }
    }

    private String renderTemplate(String templateName, Map<String, String> values) {
        String template = loadTemplate(templateName);

        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace(
                    "{{" + entry.getKey() + "}}",
                    escapeHtml(entry.getValue())
            );
        }

        return template;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String getWorkingDaysText(LeaveRequest request) {
        if (request == null) {
            return "0";
        }

        return String.valueOf(request.getWorkingDays());
    }
}