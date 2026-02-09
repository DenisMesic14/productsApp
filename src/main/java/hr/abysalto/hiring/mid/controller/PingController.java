package hr.abysalto.hiring.mid.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Pong controller
 */
@Tag(name = "Ping", description = "PONG.")
@RestController()
@RequestMapping("/ping")
public class PingController {

	@Operation(summary = "ping-pong to show app is alive", responses = {
			@ApiResponse(description = "Success", responseCode = "200", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))) })
	@GetMapping("/pong")
	public String pong() {
		return "Pong";
	}
}
