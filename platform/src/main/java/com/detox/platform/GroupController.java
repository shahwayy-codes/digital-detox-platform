package com.detox.platform;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GroupController {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final FriendGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final LeaderboardService leaderboardService;
    private final SecureRandom random = new SecureRandom();

    public GroupController(FriendGroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           UserRepository userRepository,
                           LeaderboardService leaderboardService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.leaderboardService = leaderboardService;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    private String newCode() {
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!groupRepository.existsByInviteCode(code)) {
                return code;
            }
        }
    }

    @GetMapping("/groups")
    public String list(Principal principal, Model model) {
        User user = currentUser(principal);
        List<GroupMember> memberships = groupMemberRepository.findByUser(user);
        Map<Long, Long> counts = new HashMap<>();
        for (GroupMember m : memberships) {
            counts.put(m.getGroup().getId(), groupMemberRepository.countByGroup(m.getGroup()));
        }
        model.addAttribute("memberships", memberships);
        model.addAttribute("counts", counts);
        return "groups";
    }

    @PostMapping("/groups/create")
    public String create(@RequestParam(required = false) String name,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        String clean = name == null ? "" : name.trim();
        if (clean.length() < 3 || clean.length() > 40) {
            redirectAttributes.addFlashAttribute("error", "Group name must be 3 to 40 characters.");
            return "redirect:/groups";
        }
        User user = currentUser(principal);
        FriendGroup group = new FriendGroup();
        group.setName(clean);
        group.setOwner(user);
        group.setInviteCode(newCode());
        groupRepository.save(group);
        groupMemberRepository.save(new GroupMember(group, user));

        redirectAttributes.addFlashAttribute("message", "Group created. Share the invite code with your friends.");
        return "redirect:/groups/" + group.getId();
    }

    @PostMapping("/groups/join")
    public String join(@RequestParam(required = false) String code,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {
        String clean = code == null ? "" : code.trim().toUpperCase();
        FriendGroup group = groupRepository.findByInviteCode(clean).orElse(null);
        if (group == null) {
            redirectAttributes.addFlashAttribute("error", "No group found with that code.");
            return "redirect:/groups";
        }
        User user = currentUser(principal);
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            redirectAttributes.addFlashAttribute("message", "You are already in this group.");
        } else {
            groupMemberRepository.save(new GroupMember(group, user));
            redirectAttributes.addFlashAttribute("message", "You joined the group.");
        }
        return "redirect:/groups/" + group.getId();
    }

    @GetMapping("/groups/{id}")
    public String details(@PathVariable Long id,
                          Principal principal,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        FriendGroup group = groupRepository.findById(id).orElse(null);
        if (group == null || !groupMemberRepository.existsByGroupAndUser(group, user)) {
            redirectAttributes.addFlashAttribute("error", "Group not found.");
            return "redirect:/groups";
        }
        model.addAttribute("group", group);
        model.addAttribute("entries", leaderboardService.weekly(group, user));
        model.addAttribute("isOwner", group.getOwner().getId().equals(user.getId()));
        return "group-details";
    }

    @PostMapping("/groups/{id}/leave")
    public String leave(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        FriendGroup group = groupRepository.findById(id).orElse(null);
        if (group != null && !group.getOwner().getId().equals(user.getId())) {
            groupMemberRepository.findByGroupAndUser(group, user).ifPresent(groupMemberRepository::delete);
            redirectAttributes.addFlashAttribute("message", "You left the group.");
        }
        return "redirect:/groups";
    }

    @PostMapping("/groups/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        FriendGroup group = groupRepository.findById(id).orElse(null);
        if (group != null && group.getOwner().getId().equals(user.getId())) {
            groupMemberRepository.deleteAll(groupMemberRepository.findByGroup(group));
            groupRepository.delete(group);
            redirectAttributes.addFlashAttribute("message", "Group deleted.");
        }
        return "redirect:/groups";
    }
}
