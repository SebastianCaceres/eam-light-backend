package ch.cern.cmms.eamlightweb.tools;

import ch.cern.eam.wshub.core.client.InforClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

public class EAMLightNativeRestController {

    @Autowired
    protected AuthenticationTools authenticationTools;
    @Autowired
    protected InforClient inforClient;

    /**
     * Return an object included within the standard WS Hub response, with a OK (200) HTTP status
     * @param Result
     * @param <T>
     * @return
     */
    public <T> ResponseEntity<?> ok(T Result) {
        return ResponseEntity.ok(EAMNativeResponse.fromData(Result));
    }

    /**
     * Return an object included within the standard WS Hub response, with a SERVER_ERROR HTTP status
     * @param exception
     * @return
     */
    public ResponseEntity<?> serverError(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EAMNativeResponse.fromException(exception));
    }

    /**
     * Return an object included within the standard WS Hub response, with a BAD_REQUEST HTTP status
     * @param exception
     * @return
     */
    public ResponseEntity<?> badRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EAMNativeResponse.fromException(exception));
    }

}
