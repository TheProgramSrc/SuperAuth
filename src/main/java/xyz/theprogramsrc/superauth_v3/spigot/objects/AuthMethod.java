package xyz.theprogramsrc.superauth_v3.spigot.objects;

public enum AuthMethod {
    DIALOG,
    GUI,
    COMMANDS

    ;

    public static AuthMethod of(String name){
        if(name == null) return DIALOG;
        if(name.equals(" ") || name.equals("") || name.isEmpty()) return DIALOG;
        try{
            return valueOf(name);
        }catch (Exception ex){
            return DIALOG;
        }
    }
}
