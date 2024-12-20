package ram.talia.hexal.api.casting.eval.env

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.common.entities.BaseCastingWisp
import java.util.function.Predicate

class WispCastEnv(val wisp: BaseCastingWisp, level: ServerLevel) : CastingEnvironment(level) {
    override fun getCastingEntity(): LivingEntity? = wisp.caster

    override fun getMishapEnvironment(): MishapEnvironment = WispMishapEnv(wisp, world)

    override fun postExecution(result: CastResult) {
        super.postExecution(result)
        // TODO
    }

    override fun mishapSprayPos(): Vec3 = wisp.position()

    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long {
        val mediaAvailable = wisp.media
        val mediaToTake: Long = cost.coerceAtMost(mediaAvailable)
        if(!simulate) {
            wisp.addMedia(-mediaToTake)
        }
        return cost - mediaToTake
    }

    override fun isVecInRangeEnvironment(vec: Vec3): Boolean {
        val caster = castingEntity as? ServerPlayer
        if (caster != null) {
            val sentinel = HexAPI.instance().getSentinel(caster)
            if (sentinel != null && sentinel.extendsRange() && caster.level().dimension() === sentinel.dimension() && vec.distanceToSqr(sentinel.position()) <= PlayerBasedCastEnv.SENTINEL_RADIUS * PlayerBasedCastEnv.SENTINEL_RADIUS) {
                return true
            }
        }

        return vec.distanceToSqr(wisp.position()) <= wisp.maxSqrCastingDistance()
    }

    override fun hasEditPermissionsAtEnvironment(pos: BlockPos): Boolean
        = caster?.gameMode?.gameModeForPlayer != GameType.ADVENTURE && caster?.let { world.mayInteract(it, pos) } ?: true

    override fun getCastingHand(): InteractionHand = InteractionHand.MAIN_HAND

    override fun getUsableStacks(mode: StackDiscoveryMode): MutableList<ItemStack> {
        return mutableListOf() // TODO
    }

    override fun getPrimaryStacks(): MutableList<HeldItemInfo> {
        return mutableListOf() // TODO

    }

    override fun replaceItem(stackOk: Predicate<ItemStack>, replaceWith: ItemStack, hand: InteractionHand?): Boolean {
        return false // TODO
    }

    override fun getPigment(): FrozenPigment = wisp.pigment()

    override fun setPigment(pigment: FrozenPigment?): FrozenPigment? = pigment?.let { wisp.setPigment(it) }

    override fun produceParticles(particles: ParticleSpray, pigment: FrozenPigment) {
        particles.sprayParticles(world, pigment)
    }

    override fun printMessage(message: Component) {
        (castingEntity as? ServerPlayer)?.sendSystemMessage(message)
    }
}