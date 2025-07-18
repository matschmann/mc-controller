package com.example.musiccast.controller;

import com.example.musiccast.model.Room;
import com.example.musiccast.model.BrowserItem;
import com.example.musiccast.service.MusicCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@Controller
public class MusicCastController {

    @Autowired
    private MusicCastService musicCastService;

    @GetMapping("/")
    public String index(Model model) {
        Map<String, Room> rooms = musicCastService.getAllRooms();
        model.addAttribute("rooms", rooms);
        return "index";
    }

    @PostMapping("/power/{roomName}")
    @ResponseBody
    public String togglePower(@PathVariable String roomName) {
        Room room = musicCastService.getRoom(roomName);
        if (room != null) {
            boolean newPowerState = !room.isPowerOn();
            boolean success = musicCastService.setPower(roomName, newPowerState);
            return success ? "success" : "error";
        }
        return "error";
    }

    @PostMapping("/volume/{roomName}/{direction}")
    @ResponseBody
    public String adjustVolume(@PathVariable String roomName, @PathVariable String direction) {
        boolean success = musicCastService.adjustVolume(roomName, direction);
        return success ? "success" : "error";
    }

    @PostMapping("/volume/{roomName}")
    @ResponseBody
    public String setVolume(@PathVariable String roomName, @RequestParam int volume) {
        boolean success = musicCastService.setVolume(roomName, volume);
        return success ? "success" : "error";
    }

    @PostMapping("/playback/{roomName}/{action}")
    @ResponseBody
    public String controlPlayback(@PathVariable String roomName, @PathVariable String action) {
        boolean success = musicCastService.setPlayback(roomName, action);
        return success ? "success" : "error";
    }

    @PostMapping("/mute/{roomName}")
    @ResponseBody
    public String toggleMute(@PathVariable String roomName) {
        boolean success = musicCastService.toggleMute(roomName);
        return success ? "success" : "error";
    }

    @GetMapping("/status/{roomName}")
    @ResponseBody
    public Room getRoomStatus(@PathVariable String roomName) {
        return musicCastService.getRoom(roomName);
    }

    @GetMapping("/refresh")
    public String refresh() {
        return "redirect:/";
    }

    // Browser endpoints
    @GetMapping("/browser/{roomName}")
    public String showBrowser(@PathVariable String roomName, Model model) {
        Room room = musicCastService.getRoom(roomName);
        if (room == null) {
            return "redirect:/";
        }
        
        // Prepare input for browsing
        musicCastService.prepareInputForBrowsing(roomName);
        
        // Get ALL browser items (not just first 8)
        List<BrowserItem> items = musicCastService.getAllBrowserItems(roomName);
        
        model.addAttribute("room", room);
        model.addAttribute("items", items);
        model.addAttribute("totalItems", items.size());
        
        return "browser";
    }

    @GetMapping("/browser/{roomName}/list")
    @ResponseBody
    public List<BrowserItem> getBrowserList(@PathVariable String roomName, 
                                          @RequestParam(defaultValue = "0") int index,
                                          @RequestParam(defaultValue = "8") int size) {
        // For AJAX calls, still use the paginated version if needed
        if (size > 8) {
            size = 8;
        }
        return musicCastService.getBrowserList(roomName, index, size);
    }

    @GetMapping("/browser/{roomName}/all")
    @ResponseBody
    public List<BrowserItem> getAllBrowserItems(@PathVariable String roomName) {
        return musicCastService.getAllBrowserItems(roomName);
    }

    @PostMapping("/browser/{roomName}/navigate/{index}")
    @ResponseBody
    public String navigateToItem(@PathVariable String roomName, @PathVariable int index) {
        boolean success = musicCastService.navigateToItem(roomName, index);
        return success ? "success" : "error";
    }

    @PostMapping("/browser/{roomName}/back")
    @ResponseBody
    public String navigateBack(@PathVariable String roomName) {
        boolean success = musicCastService.navigateBack(roomName);
        return success ? "success" : "error";
    }

    @PostMapping("/browser/{roomName}/play/{index}")
    @ResponseBody
    public String playItem(@PathVariable String roomName, @PathVariable int index) {
        boolean success = musicCastService.playItem(roomName, index);
        return success ? "success" : "error";
    }

    @PostMapping("/browser/{roomName}/prepare")
    @ResponseBody
    public String prepareInput(@PathVariable String roomName) {
        boolean success = musicCastService.prepareInputForBrowsing(roomName);
        return success ? "success" : "error";
    }

    @GetMapping("/debug/{roomName}")
    @ResponseBody
    public Map<String, Object> debugRoom(@PathVariable String roomName) {
        return musicCastService.debugRoomConnection(roomName);
    }

    @GetMapping("/cover/{roomName}")
    @ResponseBody
    public String getCurrentTrackCover(@PathVariable String roomName) {
        String coverUrl = musicCastService.getCurrentTrackCover(roomName);
        return coverUrl != null ? coverUrl : "";
    }

    // Multiroom endpoints
    @PostMapping("/multiroom/create")
    @ResponseBody
    public String createMultiroomGroup(@RequestParam String server, @RequestParam List<String> clients) {
        boolean success = musicCastService.createMultiroomGroup(server, clients);
        return success ? "success" : "error";
    }

    @PostMapping("/multiroom/disconnect/{roomName}")
    @ResponseBody
    public String disconnectMultiroom(@PathVariable String roomName) {
        boolean success = musicCastService.disconnectMultiroom(roomName);
        return success ? "success" : "error";
    }

    @GetMapping("/multiroom/status/{roomName}")
    @ResponseBody
    public Map<String, Object> getMultiroomStatus(@PathVariable String roomName) {
        return musicCastService.getMultiroomStatus(roomName);
    }

    @GetMapping("/multiroom")
    public String showMultiroom(Model model) {
        Map<String, Room> rooms = musicCastService.getAllRooms();
        model.addAttribute("rooms", rooms);
        return "multiroom";
    }
}
