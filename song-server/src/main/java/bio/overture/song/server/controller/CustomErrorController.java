package bio.overture.song.server.controller;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.server.model.legacy.EgoIsDownException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
@RestController
public class CustomErrorController extends BasicErrorController {
  public CustomErrorController(ErrorAttributes errorAttributes,
    ErrorProperties errorProperties) {
    super(errorAttributes, errorProperties);
  }

  /* Return the error page path. */
  @Override
  public String getErrorPath() {
    return "/error";
  }

  // Handle the /error path invoke.
  @RequestMapping(value = "/error")
  /* @ResponseBody annotation will return the error page content instead of the template error page name. */
  @ResponseBody
  @Override
  public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
    return new ModelAndView();
  }

  @Override
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    val m = new HashMap<String, Object>();
    m.put("error", "Song is unhappy with the world today");

    return new ResponseEntity<Map<String, Object>>(m, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public String processError(HttpServletRequest request, WebRequest webRequest) {
    log.error("Called process error");
    val msg = "{\"error\": \"At least this error is JSON\"}";
    log.error(msg);
    return msg;
  }

  @ExceptionHandler({ ServerException.class })
  public ResponseEntity<Object> handleServerException(HttpServletRequest req,
    ServerException ex) {
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
      ex.getMessage(),
      ex);
  }

  @ExceptionHandler({ Exception.class })
  public ResponseEntity<Object> handleGeneralException(HttpServletRequest req,
    Exception ex) {
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
      "SONG INTERNAL ERROR",
      ex);
  }

  @ExceptionHandler({ ConnectException.class })
  public ResponseEntity<Object> handleConnectException(
    HttpServletRequest req,
    ResourceAccessException ex) {
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
      "SONG can't connect to Ego", ex);
  }

  @ExceptionHandler({ ResourceAccessException.class })
  public ResponseEntity<Object> handleResourceAccessException(HttpServletRequest req,
    ResourceAccessException ex) {
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
      "ResourceAccessException: %s", ex);
  }

  @ExceptionHandler({ HttpClientErrorException.class })
  public ResponseEntity<Object> handleHttpException(HttpServletRequest req,
    HttpClientErrorException ex) {
    if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
      return errorResponse(HttpStatus.UNAUTHORIZED,
        "Not Authorized: %s", ex);
    }

    return errorResponse(ex.getStatusCode(),
      "HTTP CLIENT Error: %s",
      ex);
  }

  @ExceptionHandler({ InvalidTokenException.class })
  public ResponseEntity<Object> handleTokenException(HttpServletRequest req,
    InvalidTokenException ex) {
    return errorResponse(HttpStatus.UNAUTHORIZED,
      "Token Error: %s",
      ex);
  }

  @ExceptionHandler({ AuthenticationException.class })
  public ResponseEntity<Object> handleAuthenticationException(HttpServletRequest req,
    InvalidTokenException ex) {
    return errorResponse(HttpStatus.UNAUTHORIZED,
      "Authentication Error: %s",
      ex);
  }

  @ExceptionHandler({ EgoIsDownException.class })
  public ResponseEntity<Object> handleSongDownException(
    HttpServletRequest req,
    EgoIsDownException ex) {
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
      "Ego is down: %s",
      ex);
  }

  private String jsonEscape(String text) {
    return text.replace("\"", "\\\"");
  }

  private ResponseEntity<Object> errorResponse(HttpStatus status, String msg, Exception ex) {
    log.error(format("Creating error response for exception: '%s'", ex.getMessage()));
    val json = format("{\"error_message\": \"%s\"}", msg);
    return new ResponseEntity<>(json, status);
  }

}
