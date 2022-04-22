package xyz.theprogramsrc.superauth_v3.global.vpn_blocker;

import xyz.theprogramsrc.supercoreapi.SuperPlugin;
import xyz.theprogramsrc.supercoreapi.global.networking.ConnectionBuilder;
import xyz.theprogramsrc.supercoreapi.global.networking.CustomConnection;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.libs.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;

public class VPNBlocker {

    private final HashMap<String, Boolean> cache;
    private final String apiEndpoint;
    private final SuperPlugin<?> plugin;
    private final boolean enabled;

    public VPNBlocker(SuperPlugin<?> plugin, boolean enabled){
        this.plugin = plugin;
        this.cache = new HashMap<>();
        this.apiEndpoint = "https://api-v2.theprogramsrc.xyz/api/superauth/vpnip/check/{IP}";
        this.enabled = enabled;
        if(enabled && Utils.isConnected()){
            try{
                CustomConnection connection = ConnectionBuilder.connect("https://api-v2.theprogramsrc.xyz/api/superauth/vpnip/status");
                if(connection.isValidResponse() && connection.isResponseNotNull()){
                    JsonObject json = connection.getResponseJson();
                    int amount = json.get("data").getAsJsonObject().get("available").getAsNumber().intValue();
                    this.plugin.log("&aSuccessfully loaded &7" + amount + "&a VPN IPs to the VPN IP Blocker.");
                    this.plugin.log("&aThis number may change in the next start and during the execution of the plugin.");
                }
            }catch(IOException e){
                this.plugin.log("&cFailed to connect to the VPNBlockerAPI");
                this.plugin.addError(e);
                e.printStackTrace();
            }
        }
    }

    public boolean isVPN(String ip){
        if(!this.enabled)
            return false;
        if(!Utils.isConnected())
            return false;
        if(ip == null)
            return false;
        if(ip.equals("") || ip.equals(" ") || ip.equals("null"))
            return false;
        if(this.cache.containsKey(ip))
            return this.cache.get(ip);
        if(this.cache.size() > 90000)
            this.cache.clear();

        String url = this.apiEndpoint.replace("{IP}", ip);
        try{
            CustomConnection connection = ConnectionBuilder.connect(url);
            if(connection.isValidResponse() && connection.isResponseNotNull()){
                JsonObject json = connection.getResponseJson();
                boolean check = json.get("data").getAsJsonObject().get("check").getAsBoolean();
                this.cache.put(ip, check);
                return check;
            }
        }catch (IOException e){
            this.plugin.addError(e);
            e.printStackTrace();
        }
        return false;
    }
}
