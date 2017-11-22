package studioes.arm.six.partskit

import android.support.annotation.DrawableRes

/**
 * Created by rebeccastecker on 11/20/17.
 */
data class Player(val username: String,
                  val color: CompassRose.RoseColor,
                  val score: Int,
                  val isHost: Boolean,
                  val isPlayer: Boolean,
                  @DrawableRes val lineShape: Int)