package xyz.redoxlabs.redeemcodes.migrations;

import xyz.redoxlabs.redeemcodes.Main;

public interface Migration {
    
    String getTargetVersion();
    
    boolean execute(Main plugin) throws Exception;
}
