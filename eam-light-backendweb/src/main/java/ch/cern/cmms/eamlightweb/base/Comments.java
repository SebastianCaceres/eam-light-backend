package ch.cern.cmms.eamlightweb.base;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.comments.entities.Comment;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RequestMapping("/comments")

public class Comments extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@GetMapping
	@RequestMapping("/")
	
	
	public ResponseEntity<?> readComments(@RequestParam("entityCode") String entityCode,
								 @RequestParam("entityKeyCode") String entityKeyCode) {
		try {
			return ok(inforClient.getCommentService().readComments(authenticationTools.getInforContext(), entityCode, entityKeyCode, null));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping
	
	
	public ResponseEntity<?> createComment(Comment comment) {
		try {
			return ok(inforClient.getCommentService().createComment(authenticationTools.getInforContext(), comment));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PutMapping
	
	
	public ResponseEntity<?> updateComment(Comment comment) {
		try {
			return ok(inforClient.getCommentService().updateComment(authenticationTools.getInforContext(), comment));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
