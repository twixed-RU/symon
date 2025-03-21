/*
 * Copyright (c) 2016 Seth J. Morabito <web@loomcom.com>
 *                    Maik Merten <maikmerten@googlemail.com>
 *                    Chelsea Wilkinson <mail@chelseawilkinson.me>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.loomcom.symon.machines;

import com.loomcom.symon.Bus;
import com.loomcom.symon.Cpu;
import com.loomcom.symon.InstructionTable;
import com.loomcom.symon.devices.*;
import com.loomcom.symon.exceptions.MemoryRangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class XT6502 implements Machine {

    private final static Logger logger = LoggerFactory.getLogger(XT6502.class.getName());

    // Constants used by the simulated system. These define the memory map.
    private static final int BUS_BOTTOM = 0x0000;
    private static final int BUS_TOP    = 0xffff;

    // 32K of RAM from $0000 - $7FFF
    private static final int MEMORY_BASE = 0x0000;
    private static final int MEMORY_SIZE = 0xF000;

    // ACIA at $DFF0-$DFF3
    private static final int ACIA_BASE = 0xdff0;

    // 8KB ROM at $E000-$FFFF
    private static final int ROM_BASE = 0xe000;
    private static final int ROM_SIZE = 0x2000;


    // The simulated peripherals
    private final Bus    bus;
    private final Cpu    cpu;
    private final Acia   acia;
    private final Memory ram;
    private       Memory rom;


    public XT6502(String romFile) throws Exception {
        this.bus = new Bus(BUS_BOTTOM, BUS_TOP);
        this.cpu = new Cpu(InstructionTable.CpuBehavior.CMOS_6502);
        this.ram = new Memory(MEMORY_BASE, MEMORY_BASE + MEMORY_SIZE - 1, false);
        this.acia = new Acia6551(ACIA_BASE);

        bus.addCpu(cpu);
        bus.addDevice(ram);
        bus.addDevice(acia);

        if (romFile != null) {
            File romImage = new File(romFile);
            if (romImage.canRead()) {
                logger.info("Loading ROM image from file {}", romImage);
                this.rom = Memory.makeROM(ROM_BASE, ROM_BASE + ROM_SIZE - 1, romImage);
            } else {
                logger.info("Default ROM file {} not found, loading empty R/W memory image.", romImage);
                this.rom = Memory.makeRAM(ROM_BASE, ROM_BASE + ROM_SIZE - 1);
            }
        } else {
            logger.info("No ROM file specified, loading empty R/W memory image.");
            this.rom = Memory.makeRAM(ROM_BASE, ROM_BASE + ROM_SIZE - 1);
        }

        bus.addDevice(rom);

    }

    @Override
    public Bus getBus() {
        return bus;
    }

    @Override
    public Cpu getCpu() {
        return cpu;
    }

    @Override
    public Memory getRam() {
        return ram;
    }

    @Override
    public Acia getAcia() {
        return acia;
    }

    @Override
    public Pia getPia() { return null; }

    @Override
    public Crtc getCrtc() { return null; }

    @Override
    public Memory getRom() {
        return rom;
    }

    public void setRom(Memory rom) throws MemoryRangeException {
        if(this.rom != null) {
            bus.removeDevice(this.rom);
        }
        this.rom = rom;
        bus.addDevice(this.rom);
    }

    @Override
    public int getRomBase() {
        return ROM_BASE;
    }

    @Override
    public int getRomSize() { return ROM_SIZE; }

    @Override
    public int getMemorySize() {
        return MEMORY_SIZE;
    }

    @Override
    public String getName() {
        return "XT6502";
    }


}
