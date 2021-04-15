package xyz.theprogramsrc.superauth.global.vpn_blocker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.supercoreapi.SuperPlugin;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;

import java.util.HashMap;

public class VPNBlocker {

    private final HashMap<String, Boolean> cache;
    private final String apiEndpoint;
    private final SuperPlugin<?> plugin;

    public VPNBlocker(SuperPlugin<?> plugin){
        this.plugin = plugin;
        this.cache = new HashMap<>();
        this.apiEndpoint = "https://api.theprogramsrc.xyz/superauth/ip_checker/?ip={IP}";
    }

    public boolean isVPN(String ip){
        if(!Utils.isConnected())
            return false;

        if(ip == null)
            return false;

        if(ip.equals("") || ip.equals(" ") || ip.equals("null") || this.cache.containsKey(ip))
            return false;

        if(this.cache.size() > 50000)
            this.cache.clear();
        String url = this.apiEndpoint.replace("{IP}", ip);
        String content = Utils.readWithInputStream(url);
        if(content == null) {
            this.plugin.log("&c" + LBase.NULL_CONTENT_RETURNED);
            return false;
        }
        JsonObject json = new JsonParser().parse(content).getAsJsonObject();
        String code = json.get("code").getAsString();
        if(!code.equals("200")){
            this.plugin.log("&c" + LBase.SERVER_ERROR_WHILE_CHECKING_IP);
            this.plugin.log("&c" + json.get("message").getAsString());
            return false;
        }else{
            boolean value = json.get("message").getAsString().equals("true");
            this.cache.put(ip, value);
            return value;
        }
    }
}
