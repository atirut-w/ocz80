import li.cil.oc.api.machine.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Architecture.Name("Zilog Z80")
public class Arch implements Architecture {
    private final Machine machine;

    public Arch(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return true;
    }

    public boolean recomputeMemory(Iterable<ItemStack> stack) {
        return true;
    }

    public boolean initialize() {
        OCZ80.logger.debug("Machine init");
        return true;
    }

    public void close() {}

    public void runSynchronized() {}

    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        return new ExecutionResult.Shutdown(false);
    }

    public void onSignal() {}

    public void onConnect() {}

    public void load(NBTTagCompound nbt) {}

    public void save(NBTTagCompound nbt) {}
}
