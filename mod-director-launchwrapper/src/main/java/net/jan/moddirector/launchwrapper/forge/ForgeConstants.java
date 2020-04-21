package net.jan.moddirector.launchwrapper.forge;

public class ForgeConstants {
    public static final String COREMODS_LOAD_PROPERTY = "fml.coreMods.load";

    public static final String CORE_MOD_MANAGER_CLASS = "net.minecraftforge.fml.relauncher.CoreModManager";
    public static final String CORE_MOD_MANAGER_CLASS_LEGACY = "cpw.mods.fml.relauncher.CoreModManager";

    public static final String MOD_ACCESS_TRANSFORMER_CLASS =
            "net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer";
    public static final String MOD_ACCESS_TRANSFORMER_CLASS_LEGACY =
            "cpw.mods.fml.common.asm.transformers.ModAccessTransformer";

    public static final String IGNORED_MODS_METHOD = "getIgnoredMods";
    public static final String IGNORED_MODS_METHOD_LEGACY = "getLoadedCoremods";

    public static final String HANDLE_CASCADING_TWEAK_METHOD = "handleCascadingTweak";
    public static final String GET_REPARSEABLE_COREMODS_METHOD = "getReparseableCoremods";
    public static final String LOAD_CORE_MOD_METHOD = "loadCoreMod";
    public static final String SORT_TWEAK_LIST_METHOD = "sortTweakList";
    public static final String ADD_JAR_METHOD = "addJar";

    public static final String CORE_PLUGIN_CONTAINS_MOD_ATTRIBUTE = "FMLCorePluginContainsFMLMod";
    public static final String TWEAK_CLASS_ATTRIBUTE = "TweakClass";
    public static final String TWEAK_ORDER_ATTRIBUTE = "TweakOrder";
    public static final String CORE_PLUGIN_ATTRIBUTE = "FMLCorePlugin";
}
