package be.florien.anyflow.feature.player

import be.florien.anyflow.injection.ActivityScope
import be.florien.anyflow.injection.ViewModelModule
import dagger.Subcomponent

@Subcomponent(modules = [ViewModelModule::class])
@ActivityScope
interface PlayerComponent {

    fun inject(playerActivity: PlayerActivity)

    @Subcomponent.Builder
    interface Builder {

        fun build(): PlayerComponent
    }

}
