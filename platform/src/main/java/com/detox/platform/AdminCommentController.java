package com.detox.platform;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminCommentController {

    private final CommentRepository commentRepository;

    public AdminCommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @GetMapping("/admin/comments")
    public String list(Model model) {
        model.addAttribute("comments", commentRepository.findByReportedTrueOrderByCreatedAtDesc());
        return "admin-comments";
    }

    @PostMapping("/admin/comments/{id}/dismiss")
    public String dismiss(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setReported(false);
            commentRepository.save(comment);
            redirectAttributes.addFlashAttribute("message", "Report dismissed.");
        }
        return "redirect:/admin/comments";
    }

    @PostMapping("/admin/comments/{id}/remove")
    public String remove(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            commentRepository.delete(comment);
            redirectAttributes.addFlashAttribute("message", "Comment removed.");
        }
        return "redirect:/admin/comments";
    }
}
