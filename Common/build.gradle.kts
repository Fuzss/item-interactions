plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-common")
}

dependencies {
    modCompileOnlyApi(sharedLibs.puzzleslib.common)
}

multiloader {
    mixins {
        mixin("AbstractContainerMenuMixin")
        clientMixin(
            "AbstractContainerScreenMixin",
            "GuiGraphicsExtractorMixin",
            "ItemStackMixin",
            "MultiPlayerGameModeMixin"
        )
    }
}
