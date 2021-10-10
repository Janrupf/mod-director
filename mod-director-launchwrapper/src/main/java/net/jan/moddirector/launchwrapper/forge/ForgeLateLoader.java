package net.jan.moddirector.launchwrapper.forge;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.manage.InstalledMod;
import net.jan.moddirector.core.manage.ModDirectorError;
import net.jan.moddirector.launchwrapper.ModDirectorTweaker;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ForgeLateLoader {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final ModDirectorTweaker directorTweaker;
    private final ModDirector director;
    private final LaunchClassLoader classLoader;
    private final List<String> loadedCoremods;
    private final List<ITweaker> modTweakers;

    private List<String> reflectiveIgnoredMods;
    private List<String> reflectiveReparsedCoremods;
    private MethodHandle handleCascadingTweakMethodHandle;
    private MethodHandle loadCoreModMethodHandle;
    private MethodHandle addUrlMethodHandle;
    private MethodHandle sortTweakListMethodHandle;
    private MethodHandle addJarMethodHandle;
    private boolean addJarRequiresAtList;

    public ForgeLateLoader(ModDirectorTweaker directorTweaker, ModDirector director, LaunchClassLoader classLoader) {
        this.directorTweaker = directorTweaker;
        this.director = director;
        this.classLoader = classLoader;
        this.loadedCoremods = new ArrayList<>();
        this.modTweakers = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public void execute() {
        for(String commandlineCoreMod :
                System.getProperty(ForgeConstants.COREMODS_LOAD_PROPERTY, "").split(",")) {
            if(!commandlineCoreMod.isEmpty()) {
                director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLaterLoader",
                        "Launchwrapper", "Ignoring coremod %s which has been loaded on the commandline",
                        commandlineCoreMod);
                loadedCoremods.add(commandlineCoreMod);
            }
        }

        if(!reflectiveSetup()) {
            return;
        }

        director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                "Launchwrapper", "Trying to late load %d mods", director.getInstalledMods().size());
        director.getInstalledMods().forEach(this::handle);

        boolean sortSucceeded = false;

        if(sortTweakListMethodHandle != null) {
            @SuppressWarnings("unchecked")
            List<ITweaker> realTweakers = (List<ITweaker>) Launch.blackboard.get("Tweaks");
            Launch.blackboard.put("Tweaks", modTweakers);

            try {
                sortTweakListMethodHandle.invoke();
                sortSucceeded = true;
            } catch(Throwable t) {
                director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", t, "Error while invoking sortTweakList method");
            }

            Launch.blackboard.put("Tweaks", realTweakers);
        }

        if(!sortSucceeded) {
            director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Mod tweak list could not be sorted, hoping the best...");
        }

        Launch.blackboard.put("LateTweakers", modTweakers);

        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        boolean deobfFound = false;

        for(int i = 0; i < tweakClasses.size(); i++) {
            if(tweakClasses.get(i).endsWith(".FMLDeobfTweaker")) {
                director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", "Found deobf tweaker at index %d," +
                                "adding after deobf tweaker after it", i);
                tweakClasses.add(i + 1, "net.jan.moddirector.launchwrapper.forge.AfterDeobfTweaker");
                deobfFound = true;
            }
        }

        if(!deobfFound) {
            director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Failed to find deobf tweaker, injecting after deobf " +
                            "tweaker at first place");
            tweakClasses.add(0, "net.jan.moddirector.launchwrapper.forge.AfterDeobfTweaker");
        }
    }

    @SuppressWarnings("unchecked")
    private boolean reflectiveSetup() {
        Class<?> coreModManagerClass;

        try {
            coreModManagerClass =
                    Class.forName(ForgeConstants.CORE_MOD_MANAGER_CLASS, false, getClass().getClassLoader());
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Found new CoreModManager at %s!",
                    ForgeConstants.CORE_MOD_MANAGER_CLASS);
        } catch(ClassNotFoundException e) {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Unable to find new CoreModManager class, trying old...");
            try {
                coreModManagerClass = Class.forName(ForgeConstants.CORE_MOD_MANAGER_CLASS_LEGACY);
            } catch(ClassNotFoundException ex) {
                director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", "Unable to find old CoreModManager class, Forge support disabled!");
                return false;
            }

            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Found old CoreModManager at %s!",
                    ForgeConstants.CORE_MOD_MANAGER_CLASS);
        }

        try {
            Method sortTweakListMethod = getMethod(new String[] {
                    ForgeConstants.SORT_TWEAK_LIST_METHOD
            }, coreModManagerClass);

            sortTweakListMethodHandle = LOOKUP.unreflect(sortTweakListMethod);
        } catch(NoSuchMethodException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to find method for sorting tweaks, loading might fail!");
        } catch(IllegalAccessException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to access method for sorting tweaks, loading might fail!");
        }

        try {
            Method getIgnoredModsMethod = getMethod(new String[] {
                    ForgeConstants.IGNORED_MODS_METHOD,
                    ForgeConstants.IGNORED_MODS_METHOD_LEGACY
            }, coreModManagerClass);

            reflectiveIgnoredMods = (List<String>) getIgnoredModsMethod.invoke(null);
        } catch(NoSuchMethodException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to find method for retrieving ignored mods, " +
                            "loading might fail!");

            reflectiveIgnoredMods = new ArrayList<>();
        } catch(IllegalAccessException | InvocationTargetException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to invoke method for retrieving ignored mods, " +
                            "loading might fal!");

            reflectiveIgnoredMods = new ArrayList<>();
        }

        try {
            Method getReparseableCoremodsMethod = getMethod(new String[] {
                    ForgeConstants.GET_REPARSEABLE_COREMODS_METHOD
            }, coreModManagerClass);

            reflectiveReparsedCoremods = (List<String>) getReparseableCoremodsMethod.invoke(null);
        } catch(NoSuchMethodException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to find method for retrieving reparseable coremods, " +
                            "loading might fail!");

            reflectiveReparsedCoremods = new ArrayList<>();
        } catch(IllegalAccessException | InvocationTargetException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to invoke method for retrieving reparseable coremods, " +
                            "loading might fal!");

            reflectiveReparsedCoremods = new ArrayList<>();
        }

        try {
            Method handleCascadingTweakMethod = getMethod(new String[] {
                    ForgeConstants.HANDLE_CASCADING_TWEAK_METHOD
            }, coreModManagerClass, File.class, JarFile.class, String.class, LaunchClassLoader.class, Integer.class);

            handleCascadingTweakMethodHandle = LOOKUP.unreflect(handleCascadingTweakMethod);
        } catch(NoSuchMethodException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to find method for adding tweakers via FML, " +
                            "loading might fail, but trying to fall back to Launchwrapper directly!");
        } catch(IllegalAccessException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to access method for adding tweakers via FML, " +
                            "loading might fail, but trying to fall back to Launchwrapper directly!");
        }

        try {
            Method loadCoreModMethod = getMethod(new String[] {
                    ForgeConstants.LOAD_CORE_MOD_METHOD
            }, coreModManagerClass, LaunchClassLoader.class, String.class, File.class);

            loadCoreModMethodHandle = LOOKUP.unreflect(loadCoreModMethod);
        } catch(NoSuchMethodException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to find method for loading core mods via FML, " +
                            "loading might fail!");
        } catch(IllegalAccessException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to access method for loading core mods via FML, " +
                            "loading might fail!");
        }

        Class<?> modAccessTransformerClass = null;

        try {
            modAccessTransformerClass =
                    Class.forName(ForgeConstants.MOD_ACCESS_TRANSFORMER_CLASS, false, getClass().getClassLoader());
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Found new ModAccessTransformer at %s!",
                    modAccessTransformerClass.getName());
        } catch(ClassNotFoundException e) {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Unable to find new ModAccessTransformer class, trying old...");

            try {
                modAccessTransformerClass =
                        Class.forName(ForgeConstants.MOD_ACCESS_TRANSFORMER_CLASS_LEGACY, false,
                                getClass().getClassLoader());
                director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", "Found old ModAccessTransformer at %s!",
                        modAccessTransformerClass.getName());
            } catch(ClassNotFoundException classNotFoundException) {
                director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", e, "Failed to find ModAccessTransformer class even after " +
                                "trying legacy name. Access transformers for downloaded mods disabled, loading might " +
                                "fail!");
            }
        }

        if(modAccessTransformerClass != null) {
            try {
                Method addJarMethod = getMethod(new String[] {
                        ForgeConstants.ADD_JAR_METHOD
                }, modAccessTransformerClass, JarFile.class);
                addJarMethodHandle = LOOKUP.unreflect(addJarMethod);
                addJarRequiresAtList = false;
            } catch(NoSuchMethodException e) {
                Exception secondException = null;

                try {
                    Method addJarMethod = getMethod(new String[] {
                            ForgeConstants.ADD_JAR_METHOD
                    }, modAccessTransformerClass, JarFile.class, String.class);
                    addJarMethodHandle = LOOKUP.unreflect(addJarMethod);
                    addJarRequiresAtList = true;
                } catch (IllegalAccessException | NoSuchMethodException second) {
                    secondException = second;
                }

                if(addJarMethodHandle == null) {
                    director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLaterLoader",
                            "Launchwrapper", "Failed to find method for injecting access transformers, " +
                                    "loading might fail if they are required!");
                    director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLaterLoader",
                            "Launchwrapper", e, "\tFailure 1:");
                    if (secondException != null) {
                        director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLaterLoader",
                                "Launchwrapper", secondException, "\tFailure 2:");
                    }
                }
            } catch(IllegalAccessException e) {
                director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLaterLoader",
                        "Launchwrapper", e, "Failed to access method for injecting access transformers, " +
                                "loading might fail if they are required!");
            }
        }

        try {
            Method addUrlMethod = getMethod(new String[] {
                    "addURL"
            }, URLClassLoader.class, URL.class);
            addUrlMethodHandle = LOOKUP.unreflect(addUrlMethod);
        } catch(NoSuchMethodException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to find addUrl method for URLClassLoader (wtf?), " +
                            "loading might fail!");
        } catch(IllegalAccessException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to access addUrl method for URLClassLoader (wtf?), " +
                            "loading might fail!");
        }

        return true;
    }

    private Method getMethod(String[] possibleNames, Class<?> targetClass, Class<?>... args)
            throws NoSuchMethodException {
        Method method = null;

        for(String possibleName : possibleNames) {
            try {
                method = targetClass.getDeclaredMethod(possibleName, args);
            } catch(NoSuchMethodException ignored) {
            }
        }

        if(method == null) {
            throw new NoSuchMethodException("Failed to find method using names [" +
                    String.join(", ", possibleNames) + "] on class " + targetClass.getName());
        } else {
            method.setAccessible(true);
            return method;
        }
    }

    private void handle(InstalledMod mod) {
        Path injectedFile = mod.getFile();
        
        if(!mod.shouldInject()) {
            return;
        }

        reflectiveIgnoredMods.remove(injectedFile.toFile().getName());

        try(JarFile jar = new JarFile(injectedFile.toFile())) {
            Manifest manifest = jar.getManifest();

            if(manifest != null) {
                Attributes attributes = manifest.getMainAttributes();

                injectAccessTransformers(jar, manifest);

                String tweakClass;
                if((tweakClass = attributes.getValue(ForgeConstants.TWEAK_CLASS_ATTRIBUTE)) != null) {
                    int tweakOrder = 0;
                    String tweakOrderString;
                    if((tweakOrderString = attributes.getValue(ForgeConstants.TWEAK_ORDER_ATTRIBUTE)) != null) {
                        try {
                            tweakOrder = Integer.parseInt(tweakOrderString);
                        } catch(NumberFormatException e) {
                            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN,
                                    "ModDirector/ForgeLateLoader", "Launchwrapper", e,
                                    "Failed to parse tweak order for %s", injectedFile.toString());
                        }
                    }

                    injectTweaker(
                            injectedFile, jar, tweakClass, tweakOrder,
                            mod.getOptionBoolean("launchwrapperTweakerForceNext", false));
                    return;
                }

                String corePlugin;
                if((corePlugin = attributes.getValue(ForgeConstants.CORE_PLUGIN_ATTRIBUTE)) != null) {
                    injectCorePlugin(injectedFile, corePlugin);

                    if(attributes.getValue(ForgeConstants.CORE_PLUGIN_CONTAINS_MOD_ATTRIBUTE) != null) {
                        addReparseableJar(injectedFile);
                    } else {
                        addLoadedCoreMod(injectedFile);
                    }
                }
            } else {
                director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", "Downloaded file %s has no manifest!", injectedFile.toString());
            }
        } catch(IOException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to open indexed file %s as jar, ignoring",
                    injectedFile.toString());
        }
    }

    private void addReparseableJar(Path injectedFile) {
        String fileName = injectedFile.toFile().getName();
        if(!reflectiveReparsedCoremods.contains(fileName)) {
            reflectiveReparsedCoremods.add(fileName);
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Marked %s as reparseable coremod", injectedFile.toString());
        }
    }

    private void addLoadedCoreMod(Path injectedFile) {
        String filename = injectedFile.toFile().getName();
        if(!reflectiveIgnoredMods.contains(filename)) {
            reflectiveIgnoredMods.add(filename);
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Marked %s as loaded coremod", injectedFile.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private void injectTweaker(Path injectedFile, JarFile jar, String tweakerClass, Integer sortingOrder,
                               boolean forceNext) {
        URL fileUrl = null;

        try {
            fileUrl = injectedFile.toUri().toURL();
        } catch(MalformedURLException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to convert path to url, loading might fail!");
        }

        if(fileUrl != null) {
            try {
                addUrlMethodHandle.invoke(classLoader.getClass().getClassLoader(), fileUrl);
                classLoader.addURL(fileUrl);
            } catch(Throwable e) {
                director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", e, "Failed to inject tweaker url into ClassLoader, " +
                                "loading might fail!");
            }
        }

        if(forceNext) {
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Late injecting tweaker %s from %s, forcing it to be called next!",
                    tweakerClass, injectedFile.toString());

            try {
                ITweaker tweaker = (ITweaker) Class.forName(tweakerClass, true, classLoader).newInstance();
                classLoader.addClassLoaderExclusion(tweakerClass.substring(0, tweakerClass.lastIndexOf('.')));
                directorTweaker.callInjectedTweaker(tweaker);
            } catch(IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", e, "Failed to manually load tweaker so it can be injected next, " +
                                "falling back to Forge!");
                forceNext = false;
            }
        }

        if(forceNext) {
            return;
        }

        boolean injectionSucceeded = false;

        if(handleCascadingTweakMethodHandle != null) {
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Late injecting tweaker %s from %s using FML",
                    tweakerClass, injectedFile.toString());

            try {
                handleCascadingTweakMethodHandle.invoke(
                        injectedFile.toFile(),
                        jar,
                        tweakerClass,
                        classLoader,
                        sortingOrder
                );
                injectionSucceeded = true;
            } catch(Throwable e) {
                director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", e, "Error while injecting tweaker via FML, falling back to " +
                                "Launchwrapper's own mechanism!");
            }
        }

        if(!injectionSucceeded) {
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Late injecting tweaker %s from %s using Launchwrapper",
                    tweakerClass, injectedFile.toString());
            ((List<String>) Launch.blackboard.get("TweakClasses")).add(tweakerClass);
        }
    }

    private void injectCorePlugin(Path injectedFile, String coreModClass) {
        if(loadedCoremods.contains(coreModClass)) {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", "Not injecting core plugin %s from %s because it has already been!",
                    coreModClass, injectedFile.toString());
            return;
        }

        director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ForgeLateLoader",
                "Launchwrapper", "Now injecting core plugin %s from %s",
                coreModClass, injectedFile.toString());

        try {
            classLoader.addURL(injectedFile.toUri().toURL());
            Object ret = loadCoreModMethodHandle.invoke(classLoader, coreModClass, injectedFile.toFile());
            if(ret instanceof ITweaker) {
                modTweakers.add((ITweaker) ret);
            }
        } catch(Throwable e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ForgeLateLoader",
                    "Launchwrapper", e, "Failed to inject core plugin!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to inject core plugin!", e));
        }
    }

    private void injectAccessTransformers(JarFile jar, Manifest manifest) {
        if(addJarMethodHandle != null) {
            try {
                director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/ForgeLateLoader",
                        "Launchwrapper", "Added %s to possible access transformers", jar.getName());
                if(addJarRequiresAtList) {
                    String ats = manifest.getMainAttributes().getValue(ForgeConstants.FML_AT_ATTRIBUTE);
                    if(ats != null && !ats.isEmpty()) {
                        addJarMethodHandle.invoke(jar, ats);
                    }
                } else {
                    addJarMethodHandle.invoke(jar);
                }
            } catch(Throwable t) {
                director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN,
                        "ModDirector/ForgeLateLoader", "Launchwrapper", t,
                        "Failed to add jar to access transformers");
            }
        }
    }
}
