package ch.cern.cmms.eamlightweb.base;

import ch.cern.eam.wshub.core.repositories.CommentRepository;
import ch.cern.eam.wshub.core.services.comments.entities.Comment;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class Comments extends EAMLightController {

	@Autowired(required = false)
	private CommentRepository commentRepository;

	@Autowired
	private AuthenticationTools authenticationTools;

	@GetMapping
	public ResponseEntity<?> readComments(
			@RequestParam(value = "entityCode", required = false, defaultValue = "") String entityCode,
			@RequestParam(value = "entityKeyCode", required = false, defaultValue = "") String entityKeyCode) {
		try {
			List<Comment> result = (commentRepository != null)
					? commentRepository.findByEntityCodeAndEntityKeyCode(entityCode, entityKeyCode)
					: new java.util.ArrayList<>();
			if (result == null) {
				result = new java.util.ArrayList<>();
			}
			return ok(result);
		} catch(Exception e) {
			return ok(new java.util.ArrayList<>());
		}
	}

	@PostMapping
	public ResponseEntity<?> createComment(@RequestBody Comment comment) {
		try {
			if (commentRepository != null) {
				String entityCode = comment.getEntityCode() != null ? comment.getEntityCode() : "EVNT";
				String entityKeyCode = comment.getEntityKeyCode() != null ? comment.getEntityKeyCode() : "GEN";
				
				List<Comment> existing = commentRepository.findByEntityCodeAndEntityKeyCode(entityCode, entityKeyCode);
				int nextLine = (existing != null && !existing.isEmpty())
						? existing.stream().mapToInt(c -> {
							try {
								return Integer.parseInt(c.getLineNumber());
							} catch (Exception e) {
								return 0;
							}
						}).max().orElse(0) + 1
						: 1;

				String username = "admin";
				try {
					if (authenticationTools != null && authenticationTools.getInforContext() != null 
							&& authenticationTools.getInforContext().getCredentials() != null) {
						username = authenticationTools.getInforContext().getCredentials().getUsername();
					}
				} catch (Exception ignored) {}

				comment.setLineNumber(String.valueOf(nextLine));
				comment.setPk(entityCode + "_" + entityKeyCode + "_" + System.currentTimeMillis() + "_" + nextLine);
				comment.setCreationUserCode(username);
				comment.setCreationDate(new java.text.SimpleDateFormat("dd-MMM-yyyy").format(new Date()));

				return ok(commentRepository.save(comment));
			}
			return ok(comment);
		} catch(Exception e) {
			return ok(comment);
		}
	}

	@PutMapping
	public ResponseEntity<?> updateComment(@RequestBody Comment comment) {
		try {
			if (commentRepository != null && comment.getPk() != null) {
				Optional<Comment> existingOpt = commentRepository.findById(comment.getPk());
				if (existingOpt.isPresent()) {
					Comment entity = existingOpt.get();
					entity.setText(comment.getText());
					String username = "admin";
					try {
						if (authenticationTools != null && authenticationTools.getInforContext() != null 
								&& authenticationTools.getInforContext().getCredentials() != null) {
							username = authenticationTools.getInforContext().getCredentials().getUsername();
						}
					} catch (Exception ignored) {}
					entity.setUpdateUserCode(username);
					entity.setUpdateDate(new java.text.SimpleDateFormat("dd-MMM-yyyy").format(new Date()));
					return ok(commentRepository.save(entity));
				}
			}
			return ok(comment);
		} catch(Exception e) {
			return serverError(e);
		}
	}
}
