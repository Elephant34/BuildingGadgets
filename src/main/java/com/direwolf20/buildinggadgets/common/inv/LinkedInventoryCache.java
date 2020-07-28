package com.direwolf20.buildinggadgets.common.inv;

/**
 * This class is responsible for holding a cache of the remote inventories storage and serving the
 * data back to the player. Client only methods can't see inside Tiles thus we need to keep a cache that
 * the server keeps updated for us so we can then call it from the client. It's not ideal and it
 * definitely has the potential to burn servers alive.
 */
public class LinkedInventoryCache {

}
