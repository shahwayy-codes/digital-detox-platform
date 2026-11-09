package com.detox.platform;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    private final CommentRepository commentRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository,
                             ChallengeRepository challengeRepository,
                             UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @PostMapping("/challenges/{id}/comments")
    public String add(@PathVariable Long id,
                      @RequestParam String content,
                      Principal principal,
                      RedirectAttributes redirectAttributes) {
        Challenge challenge = challengeRepository.findById(id).orElse(null);
        if (challenge == null) {
            return "redirect:/challenges";
        }
        String text = content.trim();
        if (text.isEmpty() || text.length() > 500) {
            redirectAttributes.addFlashAttribute("error", "A comment must be 1 to 500 characters.");
            return "redirect:/challenges/" + id;
        }
        Comment comment = new Comment();
        comment.setChallenge(challenge);
        comment.setUser(currentUser(principal));
        comment.setContent(text);
        commentRepository.save(comment);
        redirectAttributes.addFlashAttribute("message", "Comment posted.");
        return "redirect:/challenges/" + id;
    }

    @PostMapping("/comments/{id}/report")
    public String report(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            return "redirect:/challenges";
        }
        User user = currentUser(principal);
        if (comment.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You cannot report your own comment.");
        } else {
            comment.setReported(true);
            commentRepository.save(comment);
            redirectAttributes.addFlashAttribute("message", "Comment reported. An admin will review it.");
        }
        return "redirect:/challenges/" + comment.getChallenge().getId();
    }

    @PostMapping("/comments/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            return "redirect:/challenges";
        }
        Long challengeId = comment.getChallenge().getId();
        User user = currentUser(principal);
        boolean owner = comment.getUser().getId().equals(user.getId());
        boolean admin = "ROLE_ADMIN".equals(user.getRole());
        if (owner || admin) {
            commentRepository.delete(comment);
            redirectAttributes.addFlashAttribute("message", "Comment deleted.");
        }
        return "redirect:/challenges/" + challengeId;
    }
}
