package simpleintro.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;

public class PacketCutsceneState implements IMessage {
    private boolean inCutscene;

    public PacketCutsceneState() {
    }

    public PacketCutsceneState(boolean inCutscene) {
        this.inCutscene = inCutscene;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.inCutscene = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.inCutscene);
    }

    public static class Handler implements IMessageHandler<PacketCutsceneState, IMessage> {
        @Override
        public IMessage onMessage(PacketCutsceneState message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player != null) {
                    NBTTagCompound nbt = player.getEntityData();
                    nbt.setBoolean("InCutscene", message.inCutscene);
                }
            });
            return null;
        }
    }
}
