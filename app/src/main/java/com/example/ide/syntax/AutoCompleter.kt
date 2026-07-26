package com.example.ide.syntax

import androidx.compose.runtime.Immutable

@Immutable
data class CompletionCandidate(
    val displayText: String,
    val completionText: String,
    val type: CompletionType,
    val detail: String = ""
)

enum class CompletionType {
    CLASS, METHOD, FIELD, ANNOTATION, KEYWORD, PACKAGE, FABRIC_API
}

object AutoCompleter {

    private val FABRIC_SUGGESTIONS = listOf(
        CompletionCandidate("@Inject", "@Inject(at = @At(\"HEAD\"), method = \"\")\nprivate void onMethod(CallbackInfo ci) {\n    \n}", CompletionType.ANNOTATION, "SpongePowered Mixin"),
        CompletionCandidate("@Redirect", "@Redirect(at = @At(value = \"INVOKE\", target = \"\"), method = \"\")", CompletionType.ANNOTATION, "SpongePowered Mixin"),
        CompletionCandidate("@ModifyVariable", "@ModifyVariable(at = @At(\"STORE\"), method = \"\", ordinal = 0)", CompletionType.ANNOTATION, "SpongePowered Mixin"),
        CompletionCandidate("@At", "@At(\"HEAD\")", CompletionType.ANNOTATION, "Mixin Injection Point"),
        CompletionCandidate("@Shadow", "@Shadow\nprivate ", CompletionType.ANNOTATION, "Mixin Shadow Field/Method"),
        CompletionCandidate("ModInitializer", "ModInitializer", CompletionType.CLASS, "net.fabricmc.api.ModInitializer"),
        CompletionCandidate("onInitialize", "public void onInitialize() {\n    // Initialize Fabric Mod\n}", CompletionType.METHOD, "Fabric Entrypoint"),
        CompletionCandidate("Registry.register", "Registry.register(Registries.ITEM, new Identifier(MOD_ID, \"ruby\"), RUBY_ITEM);", CompletionType.FABRIC_API, "Register Fabric Item"),
        CompletionCandidate("Identifier", "new Identifier(MOD_ID, \"\")", CompletionType.CLASS, "net.minecraft.util.Identifier"),
        CompletionCandidate("FabricItemSettings", "new FabricItemSettings()", CompletionType.CLASS, "net.fabricmc.fabric.api.item.v1.FabricItemSettings"),
        CompletionCandidate("FabricBlockSettings", "FabricBlockSettings.create()", CompletionType.CLASS, "net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings"),
        CompletionCandidate("ItemStack", "new ItemStack(Items.DIAMOND, 1)", CompletionType.CLASS, "net.minecraft.item.ItemStack"),
        CompletionCandidate("PlayerEntity", "PlayerEntity", CompletionType.CLASS, "net.minecraft.entity.player.PlayerEntity"),
        CompletionCandidate("TitleScreen", "TitleScreen.class", CompletionType.CLASS, "net.minecraft.client.gui.screen.TitleScreen"),
        CompletionCandidate("CallbackInfo", "CallbackInfo info", CompletionType.CLASS, "org.spongepowered.asm.mixin.injection.callback.CallbackInfo"),
        CompletionCandidate("LoggerFactory.getLogger", "LoggerFactory.getLogger(MOD_ID)", CompletionType.METHOD, "SLF4J Logger")
    )

    fun getSuggestions(code: String, cursorOffset: Int): List<CompletionCandidate> {
        if (cursorOffset <= 0 || cursorOffset > code.length) return emptyList()

        // Extract word behind cursor
        var start = cursorOffset - 1
        while (start >= 0 && (code[start].isLetterOrDigit() || code[start] == '@' || code[start] == '.' || code[start] == '_')) {
            start--
        }
        val prefix = code.substring(start + 1, cursorOffset).trim()

        if (prefix.isEmpty()) {
            return FABRIC_SUGGESTIONS.take(5)
        }

        return FABRIC_SUGGESTIONS.filter {
            it.displayText.contains(prefix, ignoreCase = true) ||
            it.completionText.contains(prefix, ignoreCase = true)
        }
    }
}
