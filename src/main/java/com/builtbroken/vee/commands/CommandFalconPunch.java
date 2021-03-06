package com.builtbroken.vee.commands;

import com.builtbroken.jlib.lang.TextColor;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.core.commands.prefab.AbstractCommand;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 9/18/2015.
 */
public class CommandFalconPunch extends AbstractCommand
{
    public HashMap<String, WeakReference<EntityPlayer>> poweredPlayers = new HashMap();
    public HashMap<String, Long> addTimes = new HashMap();

    public CommandFalconPunch()
    {
        super("falconpunch");
    }

    @Override
    public boolean isHelpCommand(String[] args)
    {
        return args != null && args.length > 0 && args[0] != null && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"));
    }

    @Override
    public boolean handleEntityPlayerCommand(EntityPlayer player, String[] args)
    {
        if (poweredPlayers.containsKey(player.getCommandSenderName()))
        {
            poweredPlayers.remove(player.getCommandSenderName());
            addTimes.remove(player.getCommandSenderName());
            player.addChatComponentMessage(new ChatComponentText(TextColor.RED.getColorString() + " :( no punch time"));
        }
        else
        {
            poweredPlayers.put(player.getCommandSenderName(), new WeakReference<EntityPlayer>(player));
            addTimes.put(player.getCommandSenderName(), System.currentTimeMillis());
            player.addChatComponentMessage(new ChatComponentText("Punch Time >:)"));
            MinecraftForge.EVENT_BUS.register(this);
            FMLCommonHandler.instance().bus().register(this);
        }
        return true;
    }

    @SubscribeEvent
    public void onPlayerClick(LivingAttackEvent event)
    {
        if (event.source instanceof EntityDamageSource && event.source.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.source.getEntity();
            Entity target = event.entity;

            if (poweredPlayers.containsKey(player.getCommandSenderName()))
            {
                poweredPlayers.remove(player.getCommandSenderName());
                Pos pos = new Pos(target).subtract(new Pos(player)).normalize();
                target.addVelocity(pos.x(), pos.y() + .3, pos.z());
                player.addChatComponentMessage(new ChatComponentText("Falcon Punch!!!"));
                if (target instanceof EntityPlayer)
                {
                    //TODO if player's near have client side mod installed play audio clip
                    ((EntityPlayer) target).addChatComponentMessage(new ChatComponentText("Falcon Punch!!!"));
                }

                if (event.isCancelable())
                {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.WorldTickEvent event)
    {
        if (event.world.provider.dimensionId == 0)
        {
            if (poweredPlayers.size() == 0)
            {
                addTimes.clear();
                MinecraftForge.EVENT_BUS.unregister(this);
                FMLCommonHandler.instance().bus().unregister(this);
            }
            else
            {
                Iterator<String> it = poweredPlayers.keySet().iterator();
                while (it.hasNext())
                {
                    String username = it.next();
                    if (!addTimes.containsKey(username))
                    {
                        it.remove();
                        addTimes.remove(username);
                    }
                    else if ((System.currentTimeMillis() - addTimes.get(username)) > 25000)
                    {
                        it.remove();
                        addTimes.remove(username);
                    }
                }
            }
        }
    }
}
