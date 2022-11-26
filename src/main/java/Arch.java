import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.machine.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.codingrodent.microprocessor.*;

@Architecture.Name("Zilog Z80")
public class Arch implements Architecture {
    private final Machine machine;

    private char[] ram;

    public Arch(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return true;
    }

    public boolean recomputeMemory(Iterable<ItemStack> stacks) {
        OCZ80.logger.info("Recomputing memory...");
        int total = 0;

        for (ItemStack stack : stacks) {
            DriverItem driver = Driver.driverFor(stack);
            if (driver instanceof Memory) {
                total += ((Memory)driver).amount(stack) * 1024; // 1/4 KB
            }
        }
        total /= 4;

        OCZ80.logger.info("Total memory: " + total / 1024 + "KB");

        return total > 0;
    }

    public boolean initialize() {
        OCZ80.logger.info("Machine init");
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
