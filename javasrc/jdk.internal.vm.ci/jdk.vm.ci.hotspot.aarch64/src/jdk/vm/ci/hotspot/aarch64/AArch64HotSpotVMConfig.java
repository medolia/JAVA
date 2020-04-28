/*
 * Copyright (c) 2016, 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package jdk.vm.ci.hotspot.aarch64;

import jdk.vm.ci.hotspot.HotSpotVMConfigAccess;
import jdk.vm.ci.hotspot.HotSpotVMConfigStore;
import jdk.vm.ci.services.Services;

/**
 * Used to access native configuration details.
 *
 * All non-static, public fields in this class are so that they can be compiled as constants.
 */
class AArch64HotSpotVMConfig extends HotSpotVMConfigAccess {

    AArch64HotSpotVMConfig(HotSpotVMConfigStore config) {
        super(config);
    }

    final boolean linuxOs = Services.getSavedProperty("os.name", "").startsWith("Linux");

    final boolean useCompressedOops = getFlag("UseCompressedOops", Boolean.class);

    // CPU Capabilities

    /*
     * These flags are set based on the corresponding command line flags.
     */
    final boolean useBarriersForVolatile = getFlag("UseBarriersForVolatile", Boolean.class);
    final boolean useCRC32 = getFlag("UseCRC32", Boolean.class);
    final boolean useNeon = getFlag("UseNeon", Boolean.class);
    final boolean useSIMDForMemoryOps = getFlag("UseSIMDForMemoryOps", Boolean.class);
    final boolean avoidUnalignedAccesses = getFlag("AvoidUnalignedAccesses", Boolean.class);
    final boolean useLSE = getFlag("UseLSE", Boolean.class);
    final boolean useBlockZeroing = getFlag("UseBlockZeroing", Boolean.class);

    final long vmVersionFeatures = getFieldValue("Abstract_VM_Version::_features", Long.class, "uint64_t");

    /*
     * These flags are set if the corresponding support is in the hardware.
     */
    // Checkstyle: stop
    final long aarch64FP = getConstant("VM_Version::CPU_FP", Long.class);
    final long aarch64ASIMD = getConstant("VM_Version::CPU_ASIMD", Long.class);
    final long aarch64EVTSTRM = getConstant("VM_Version::CPU_EVTSTRM", Long.class);
    final long aarch64AES = getConstant("VM_Version::CPU_AES", Long.class);
    final long aarch64PMULL = getConstant("VM_Version::CPU_PMULL", Long.class);
    final long aarch64SHA1 = getConstant("VM_Version::CPU_SHA1", Long.class);
    final long aarch64SHA2 = getConstant("VM_Version::CPU_SHA2", Long.class);
    final long aarch64CRC32 = getConstant("VM_Version::CPU_CRC32", Long.class);
    final long aarch64LSE = getConstant("VM_Version::CPU_LSE", Long.class);
    final long aarch64STXR_PREFETCH = getConstant("VM_Version::CPU_STXR_PREFETCH", Long.class);
    final long aarch64A53MAC = getConstant("VM_Version::CPU_A53MAC", Long.class);
    final long aarch64DMB_ATOMICS = getConstant("VM_Version::CPU_DMB_ATOMICS", Long.class);
    // Checkstyle: resume
}
