package ch.cern.cmms.eamlightweb.base;

import ch.cern.cmms.eamlightejb.base.entity.CommentEntity;
import ch.cern.cmms.eamlightejb.base.entity.CommentId;
import ch.cern.cmms.eamlightejb.base.repository.CommentRepository;
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

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private AuthenticationTools authenticationTools;

	@GetMapping
	public ResponseEntity<?> readComments(@RequestParam("entityCode") String entityCode,
								 @RequestParam("entityKeyCode") String entityKeyCode) {
		try {
			return ok(commentRepository.findByEntityCodeAndKeyValueOrderByLineAsc(entityCode, entityKeyCode));
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping
	public ResponseEntity<?> createComment(@RequestBody CommentEntity comment) {
		try {
			List<CommentEntity> existing = commentRepository.findByEntityCodeAndKeyValueOrderByLineAsc(comment.getEntityCode(), comment.getKeyValue());
			int nextLine = existing.stream().mapToInt(CommentEntity::getLine).max().orElse(0) + 1;

			comment.setLine(nextLine);
			comment.setUser(authenticationTools.getInforContext().getCredentials().getUsername());
			comment.setDate(new Date());

			return ok(commentRepository.save(comment));
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PutMapping
	public ResponseEntity<?> updateComment(@RequestBody CommentEntity comment) {
		try {
			CommentId id = new CommentId(comment.getKeyValue(), comment.getEntityCode(), comment.getLine());
			Optional<CommentEntity> existingOpt = commentRepository.findById(id);
			if (existingOpt.isPresent()) {
				CommentEntity entity = existingOpt.get();
				entity.setText(comment.getText());
				entity.setUser(authenticationTools.getInforContext().getCredentials().getUsername());
				entity.setDate(new Date());
				return ok(commentRepository.save(entity));
			} else {
				return badRequest(new Exception("Comment not found for update"));
			}
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
