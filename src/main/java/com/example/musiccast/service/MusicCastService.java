package com.example.musiccast.service;

import com.example.musiccast.model.Room;
import com.example.musiccast.model.BrowserItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class MusicCastService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Room> rooms;

    public MusicCastService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.rooms = new HashMap<>();
        
        // Initialize rooms with your IP addresses
        rooms.put("Wohnzimmer", new Room("Wohnzimmer", "192.168.178.21"));
        rooms.put("Schlafzimmer", new Room("Schlafzimmer", "192.168.178.31"));
        rooms.put("Küche", new Room("Küche", "192.168.178.23")); // Assuming different IP for Küche
    }

    public Map<String, Room> getAllRooms() {
        // Update status for all rooms
        for (Room room : rooms.values()) {
            updateRoomStatus(room);
        }
        return rooms;
    }

    public Room getRoom(String roomName) {
        Room room = rooms.get(roomName);
        if (room != null) {
            updateRoomStatus(room);
        }
        return room;
    }

    private void updateRoomStatus(Room room) {
        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/%s/getStatus", 
                                     room.getIpAddress(), room.getZone());
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                
                // Update power status
                if (jsonNode.has("power")) {
                    room.setPowerOn("on".equals(jsonNode.get("power").asText()));
                }
                
                // Update volume
                if (jsonNode.has("volume")) {
                    room.setVolume(jsonNode.get("volume").asInt());
                }
                
                // Update mute status
                if (jsonNode.has("mute")) {
                    room.setMuted(jsonNode.get("mute").asBoolean());
                }
            }
            
            // Get playing info
            getPlayingInfo(room);
            
        } catch (Exception e) {
            System.err.println("Error updating status for room " + room.getName() + ": " + e.getMessage());
        }
    }

    private void getPlayingInfo(Room room) {
        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/netusb/getPlayInfo", 
                                     room.getIpAddress());
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                
                if (jsonNode.has("playback")) {
                    room.setPlaybackStatus(jsonNode.get("playback").asText());
                }
                
                if (jsonNode.has("track")) {
                    room.setCurrentTrack(jsonNode.get("track").asText());
                } else if (jsonNode.has("artist") && jsonNode.has("album")) {
                    String track = String.format("%s - %s", 
                                                jsonNode.get("artist").asText(),
                                                jsonNode.get("album").asText());
                    room.setCurrentTrack(track);
                }
            }
        } catch (Exception e) {
            // Ignore errors for playing info as device might not be playing anything
        }
    }

    public boolean setPower(String roomName, boolean powerOn) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String powerState = powerOn ? "on" : "standby";
            String url = String.format("http://%s/YamahaExtendedControl/v1/%s/setPower?power=%s", 
                                     room.getIpAddress(), room.getZone(), powerState);
            restTemplate.getForObject(url, String.class);
            room.setPowerOn(powerOn);
            return true;
        } catch (Exception e) {
            System.err.println("Error setting power for room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean setVolume(String roomName, int volume) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/%s/setVolume?volume=%d", 
                                     room.getIpAddress(), room.getZone(), volume);
            restTemplate.getForObject(url, String.class);
            room.setVolume(volume);
            return true;
        } catch (Exception e) {
            System.err.println("Error setting volume for room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean adjustVolume(String roomName, String direction) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/%s/setVolume?volume=%s&step=5", 
                                     room.getIpAddress(), room.getZone(), direction);
            restTemplate.getForObject(url, String.class);
            updateRoomStatus(room);
            return true;
        } catch (Exception e) {
            System.err.println("Error adjusting volume for room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean setPlayback(String roomName, String action) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/netusb/setPlayback?playback=%s", 
                                     room.getIpAddress(), action);
            restTemplate.getForObject(url, String.class);
            room.setPlaybackStatus(action);
            return true;
        } catch (Exception e) {
            System.err.println("Error setting playback for room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean toggleMute(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            boolean newMuteState = !room.isMuted();
            String url = String.format("http://%s/YamahaExtendedControl/v1/%s/setMute?enable=%s", 
                                     room.getIpAddress(), room.getZone(), newMuteState);
            restTemplate.getForObject(url, String.class);
            room.setMuted(newMuteState);
            return true;
        } catch (Exception e) {
            System.err.println("Error toggling mute for room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    // Browser functionality methods
    public boolean prepareInputForBrowsing(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/main/prepareInputChange?input=server", 
                                     room.getIpAddress());
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("Error preparing input for browsing in room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public List<BrowserItem> getBrowserList(String roomName, int index, int size) {
        Room room = rooms.get(roomName);
        List<BrowserItem> items = new ArrayList<>();
        
        if (room == null) {
            System.err.println("Room not found: " + roomName);
            return items;
        }

        // Ensure size doesn't exceed maximum of 8
        if (size > 8) {
            size = 8;
        }

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/netusb/getListInfo?list_id=main&input=server&index=%d&size=%d&lang=en", 
                                     room.getIpAddress(), index, size);
            System.out.println("Requesting browser list from: " + url);
            
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Browser list response: " + response);
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                
                // Check for error response
                if (jsonNode.has("response_code")) {
                    int responseCode = jsonNode.get("response_code").asInt();
                    System.out.println("Response code: " + responseCode);
                    if (responseCode != 0) {
                        System.err.println("MusicCast API returned error code: " + responseCode);
                        return items;
                    }
                }
                
                if (jsonNode.has("list_info") && jsonNode.get("list_info").isArray()) {
                    JsonNode listInfo = jsonNode.get("list_info");
                    System.out.println("Found " + listInfo.size() + " items in list");
                    
                    for (int i = 0; i < listInfo.size(); i++) {
                        JsonNode item = listInfo.get(i);
                        
                        BrowserItem browserItem = new BrowserItem();
                        browserItem.setIndex(index + i);
                        
                        // Debug: Print all available fields in the item
                        System.out.println("=== Item " + i + " Debug Info ===");
                        item.fieldNames().forEachRemaining(fieldName -> {
                            System.out.println("Field: " + fieldName + " = " + item.get(fieldName).asText());
                        });
                        
                        if (item.has("text")) {
                            browserItem.setText(item.get("text").asText());
                        }
                        
                        if (item.has("attribute")) {
                            int attribute = item.get("attribute").asInt();
                            browserItem.setAttribute(String.valueOf(attribute));
                            
                            // Determine if it's a container (folder) or playable item
                            // Based on MusicCast API documentation:
                            // attribute & 0x01 = playable
                            // attribute & 0x02 = container
                            browserItem.setPlayable((attribute & 0x01) != 0);
                            browserItem.setContainer((attribute & 0x02) != 0);
                            
                            System.out.println("Item: " + browserItem.getText() + 
                                             ", Attribute: " + attribute + 
                                             ", Playable: " + browserItem.isPlayable() + 
                                             ", Container: " + browserItem.isContainer());
                        }
                        
                        // Try multiple possible cover URL fields
                        String[] possibleCoverFields = {"albumart_url", "thumbnail", "image_url", "cover_art", "artwork_url", "album_art"};
                        for (String field : possibleCoverFields) {
                            if (item.has(field)) {
                                String coverUrl = item.get(field).asText();
                                if (!coverUrl.isEmpty() && !coverUrl.equals("")) {
                                    browserItem.setAlbumartUrl(coverUrl);
                                    System.out.println("Found cover in field '" + field + "': " + coverUrl);
                                    break;
                                }
                            }
                        }
                        
                        items.add(browserItem);
                    }
                } else {
                    System.err.println("No list_info found in response or it's not an array");
                }
            } else {
                System.err.println("Empty response from MusicCast API");
            }
        } catch (Exception e) {
            System.err.println("Error getting browser list for room " + roomName + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Returning " + items.size() + " browser items");
        return items;
    }

    public List<BrowserItem> getAllBrowserItems(String roomName) {
        List<BrowserItem> allItems = new ArrayList<>();
        int currentIndex = 0;
        int pageSize = 8;
        boolean hasMoreItems = true;
        
        System.out.println("Loading all browser items for room: " + roomName);
        
        while (hasMoreItems) {
            List<BrowserItem> pageItems = getBrowserList(roomName, currentIndex, pageSize);
            
            if (pageItems.isEmpty()) {
                hasMoreItems = false;
                System.out.println("No more items found at index " + currentIndex);
            } else {
                allItems.addAll(pageItems);
                currentIndex += pageItems.size();
                
                // If we got less than pageSize items, we've reached the end
                if (pageItems.size() < pageSize) {
                    hasMoreItems = false;
                    System.out.println("Reached end of list with " + pageItems.size() + " items in last batch");
                } else {
                    System.out.println("Loaded " + pageItems.size() + " items, continuing from index " + currentIndex);
                    
                    // Small delay to avoid overwhelming the API
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            // Safety check to prevent infinite loops
            if (currentIndex > 1000) {
                System.err.println("Safety limit reached, stopping at index " + currentIndex);
                break;
            }
        }
        
        System.out.println("Total items loaded: " + allItems.size());
        return allItems;
    }

    public String getCurrentTrackCover(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) return null;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/netusb/getPlayInfo", 
                                     room.getIpAddress());
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                
                // Debug: Print all available fields
                System.out.println("=== Current Track Debug Info ===");
                jsonNode.fieldNames().forEachRemaining(fieldName -> {
                    System.out.println("Field: " + fieldName + " = " + jsonNode.get(fieldName).asText());
                });
                
                // Try multiple possible cover URL fields
                String[] possibleCoverFields = {"albumart_url", "thumbnail", "image_url", "cover_art", "artwork_url", "album_art"};
                for (String field : possibleCoverFields) {
                    if (jsonNode.has(field)) {
                        String coverUrl = jsonNode.get(field).asText();
                        if (!coverUrl.isEmpty() && !coverUrl.equals("")) {
                            System.out.println("Found current track cover in field '" + field + "': " + coverUrl);
                            return coverUrl;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting current track cover for room " + roomName + ": " + e.getMessage());
        }
        
        return null;
    }

    public boolean navigateToItem(String roomName, int index) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/netusb/setListControl?list_id=main&type=select&index=%d&zone=main", 
                                     room.getIpAddress(), index);
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("Error navigating to item in room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean navigateBack(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/netusb/setListControl?list_id=main&type=return&zone=main", 
                                     room.getIpAddress());
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("Error navigating back in room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean playItem(String roomName, int index) {
        Room room = rooms.get(roomName);
        if (room == null) return false;

        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/netusb/setListControl?list_id=main&type=play&index=%d&zone=main", 
                                     room.getIpAddress(), index);
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("Error playing item in room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> debugRoomConnection(String roomName) {
        Map<String, Object> debug = new HashMap<>();
        Room room = rooms.get(roomName);
        
        if (room == null) {
            debug.put("error", "Room not found");
            return debug;
        }
        
        debug.put("roomName", roomName);
        debug.put("ipAddress", room.getIpAddress());
        
        // Test basic connectivity
        try {
            String statusUrl = String.format("http://%s/YamahaExtendedControl/v1/system/getDeviceInfo", room.getIpAddress());
            String response = restTemplate.getForObject(statusUrl, String.class);
            debug.put("deviceInfo", response);
            debug.put("connectivity", "OK");
        } catch (Exception e) {
            debug.put("connectivity", "FAILED");
            debug.put("connectivityError", e.getMessage());
        }
        
        // Test input preparation
        try {
            String prepareUrl = String.format("http://%s/YamahaExtendedControl/v1/main/prepareInputChange?input=server", room.getIpAddress());
            String response = restTemplate.getForObject(prepareUrl, String.class);
            debug.put("inputPreparation", response);
        } catch (Exception e) {
            debug.put("inputPreparationError", e.getMessage());
        }
        
        // Test list info
        try {
            String listUrl = String.format("http://%s/YamahaExtendedControl/v1/netusb/getListInfo?list_id=main&input=server&index=0&size=8&lang=en", room.getIpAddress());
            String response = restTemplate.getForObject(listUrl, String.class);
            debug.put("listInfo", response);
        } catch (Exception e) {
            debug.put("listInfoError", e.getMessage());
        }
        
        return debug;
    }

    // Multiroom functionality
    public boolean createMultiroomGroup(String serverRoomName, List<String> clientRoomNames) {
        Room serverRoom = rooms.get(serverRoomName);
        if (serverRoom == null) {
            System.err.println("Server room not found: " + serverRoomName);
            return false;
        }

        try {
            // Generate a unique group ID
            String groupId = java.util.UUID.randomUUID().toString().replace("-", "");
            
            // Step 1: Set server info
            String serverUrl = String.format("http://%s/YamahaExtendedControl/v1/dist/setServerInfo", 
                                           serverRoom.getIpAddress());
            
            StringBuilder clientListJson = new StringBuilder("[");
            for (int i = 0; i < clientRoomNames.size(); i++) {
                Room clientRoom = rooms.get(clientRoomNames.get(i));
                if (clientRoom != null) {
                    if (i > 0) clientListJson.append(",");
                    clientListJson.append("\"").append(clientRoom.getIpAddress()).append("\"");
                }
            }
            clientListJson.append("]");
            
            String serverPayload = String.format("{\"client_list\":%s,\"group_id\":\"%s\",\"type\":\"add\",\"zone\":\"main\"}", 
                                               clientListJson.toString(), groupId);
            
            System.out.println("Setting server info: " + serverPayload);
            
            // Use POST request with JSON payload
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<String> serverEntity = new org.springframework.http.HttpEntity<>(serverPayload, headers);
            
            restTemplate.postForObject(serverUrl, serverEntity, String.class);
            
            // Step 2: Set client info for each client
            for (String clientRoomName : clientRoomNames) {
                Room clientRoom = rooms.get(clientRoomName);
                if (clientRoom != null) {
                    String clientUrl = String.format("http://%s/YamahaExtendedControl/v1/dist/setClientInfo", 
                                                   clientRoom.getIpAddress());
                    
                    String clientPayload = String.format("{\"group_id\":\"%s\",\"server_ip_address\":\"%s\",\"zone\":[\"main\"]}", 
                                                       groupId, serverRoom.getIpAddress());
                    
                    System.out.println("Setting client info for " + clientRoomName + ": " + clientPayload);
                    
                    org.springframework.http.HttpEntity<String> clientEntity = new org.springframework.http.HttpEntity<>(clientPayload, headers);
                    restTemplate.postForObject(clientUrl, clientEntity, String.class);
                }
            }
            
            // Step 3: Start distribution
            String startUrl = String.format("http://%s/YamahaExtendedControl/v1/dist/startDistribution?num=0", 
                                          serverRoom.getIpAddress());
            restTemplate.getForObject(startUrl, String.class);
            
            // Step 4: Set group name
            String groupName = serverRoomName + " + " + String.join(" + ", clientRoomNames);
            String groupNameUrl = String.format("http://%s/YamahaExtendedControl/v1/dist/setGroupName", 
                                              serverRoom.getIpAddress());
            String groupNamePayload = String.format("{\"name\":\"%s\"}", groupName);
            
            org.springframework.http.HttpEntity<String> groupNameEntity = new org.springframework.http.HttpEntity<>(groupNamePayload, headers);
            restTemplate.postForObject(groupNameUrl, groupNameEntity, String.class);
            
            System.out.println("Multiroom group created successfully: " + groupName);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error creating multiroom group: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean disconnectMultiroom(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) {
            System.err.println("Room not found: " + roomName);
            return false;
        }

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            // Step 1: Clear client info
            String clientUrl = String.format("http://%s/YamahaExtendedControl/v1/dist/setClientInfo", 
                                           room.getIpAddress());
            String clientPayload = "{\"group_id\":\"\",\"server_ip_address\":\"\"}";
            
            org.springframework.http.HttpEntity<String> clientEntity = new org.springframework.http.HttpEntity<>(clientPayload, headers);
            restTemplate.postForObject(clientUrl, clientEntity, String.class);
            
            // Step 2: Clear server info
            String serverUrl = String.format("http://%s/YamahaExtendedControl/v1/dist/setServerInfo", 
                                           room.getIpAddress());
            String serverPayload = "{\"group_id\":\"\",\"zone\":\"main\"}";
            
            org.springframework.http.HttpEntity<String> serverEntity = new org.springframework.http.HttpEntity<>(serverPayload, headers);
            restTemplate.postForObject(serverUrl, serverEntity, String.class);
            
            // Step 3: Clear group name
            String groupNameUrl = String.format("http://%s/YamahaExtendedControl/v1/dist/setGroupName", 
                                              room.getIpAddress());
            String groupNamePayload = "{\"name\":\"\"}";
            
            org.springframework.http.HttpEntity<String> groupNameEntity = new org.springframework.http.HttpEntity<>(groupNamePayload, headers);
            restTemplate.postForObject(groupNameUrl, groupNameEntity, String.class);
            
            System.out.println("Multiroom disconnected for room: " + roomName);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error disconnecting multiroom for room " + roomName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> getMultiroomStatus(String roomName) {
        Room room = rooms.get(roomName);
        Map<String, Object> status = new HashMap<>();
        
        if (room == null) {
            status.put("error", "Room not found");
            return status;
        }
        
        try {
            String url = String.format("http://%s/YamahaExtendedControl/v1/dist/getDistributionInfo", 
                                     room.getIpAddress());
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                status.put("raw_response", response);
                
                if (jsonNode.has("group_id")) {
                    status.put("group_id", jsonNode.get("group_id").asText());
                }
                if (jsonNode.has("group_name")) {
                    status.put("group_name", jsonNode.get("group_name").asText());
                }
                if (jsonNode.has("role")) {
                    status.put("role", jsonNode.get("role").asText());
                }
                if (jsonNode.has("server_zone")) {
                    status.put("server_zone", jsonNode.get("server_zone").asText());
                }
                if (jsonNode.has("client_list")) {
                    status.put("client_list", jsonNode.get("client_list"));
                }
            }
            
        } catch (Exception e) {
            status.put("error", e.getMessage());
            System.err.println("Error getting multiroom status for room " + roomName + ": " + e.getMessage());
        }
        
        return status;
    }
}
