package xyz.theprogramsrc.superauth.global;

import xyz.theprogramsrc.supercoreapi.global.LogsFilter;

public class CommandFilter extends LogsFilter {

    public CommandFilter(String... filteredWords){
        super(FilterResult.DENY, filteredWords);
    }

    @Override
    public String[] getExtraRequirements() {
        return new String[]{
                "issued server command",
                "issued",
                "command",
        };
    }
}
