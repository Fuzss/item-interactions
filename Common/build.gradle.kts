plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-common")
}

dependencies {
    modCompileOnlyApi(sharedLibs.puzzleslib.common)
}

multiloader {
    mixins {
        mixin("ItemStackMixin")
        clientMixin("AbstractContainerScreenMixin", "GuiGraphicsExtractorMixin", "MultiPlayerGameModeMixin")
    }
}
