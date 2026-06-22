package simpleintro.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.entity.EntityPlayerSP;

public class PacketSeenCutsceneState implements IMessage {
    private boolean seenCutscene;

    public PacketSeenCutsceneState() {
    }

    public PacketSeenCutsceneState(boolean seenCutscene) {
        this.seenCutscene = seenCutscene;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.seenCutscene = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.seenCutscene);
    }

    public static class Handler implements IMessageHandler<PacketSeenCutsceneState, IMessage> {
        @Override
        public IMessage onMessage(PacketSeenCutsceneState message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    EntityPlayerSP player = Minecraft.getMinecraft().player;
                    if (player != null) {
                        NBTTagCompound nbt = player.getEntityData();
                        nbt.setBoolean("SeenCutscene", message.seenCutscene);
                    }
                });
            }
            return null;
        }
    }
}
