# What?

You might be a little bit frightened right now. Why does your mod contain classes in the `net.minecraft` package, you're probably asking.

The short answer is: Because there's no other way to patch some things in the game.

The longer answer is: Because it's currently impossible to access or even reference package-private types in Mixins, and creating
proxy interfaces in the `net.minecraft` package allows me to get around this. Until Fabric sees Access Transformers or Mixins
allows package-private access, this horrible hack will always be required.

The class names have been prefixed with `Lithum_` to avoid name conflicts. There _should_ be no way this can cause issues.

If you have a better solution on how to handle this, please consider opening an issue with details. I'm very receptive to alternative
workarounds.