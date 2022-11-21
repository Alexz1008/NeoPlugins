package me.neoblade298.neomythicextension.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.bukkit.utils.lib.lang3.Validate;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.damage.DamagingMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.element.ElementalDamagePacket;
import io.lumine.mythic.lib.util.DefenseFormula;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@MythicMechanic(
        author = "Indyuce",
        name = "mmodamage",
        aliases = {"mmod"},
        description = "Deals damage to the target (compatible with MMO plugins)"
)
public class ElementDamage extends DamagingMechanic implements ITargetedEntitySkill {
    protected final PlaceholderDouble amount;
    protected final boolean ignoreMMOAttack;
    private static final String
    ATTACK_METADATA_TAG = "AttackMetadata";
    private static final AttributeModifier NO_KNOCKBACK = new AttributeModifier(UUID.randomUUID(), "noKnockback", 100, AttributeModifier.Operation.ADD_NUMBER);

    private static final double MINIMUM_DAMAGE = .001;



    /**
     * Can be empty if no damage type is registered.
     * <p>
     * It IS possible but any attack should be at least physical or magical.
     * It should also be either a weapon/skill/unarmed attack.
     */
    protected final DamageType[] types;

    /**
     * Cannot save the Element object reference since skills
     * load BEFORE elements. This also permits the elements to
     * be modified without having to reload skills which reduces
     * MythicLib module load inter-dependency.
     */
    @Nullable
    private final String elementName;

    public ElementDamage(SkillExecutor manager, String line, MythicLineConfig config) {
        super(manager, line, config);

        this.amount = PlaceholderDouble.of(config.getString(new String[]{"amount", "a"}, "1", new String[0]));
        String typesString = config.getString(new String[]{"type", "t", "types"}, null, new String[0]);
        this.ignoreMMOAttack = config.getBoolean(new String[]{"ignoreMMOAttack", "immo"}, false);
        this.elementName = config.getString(new String[]{"element", "el", "e"}, null);
        this.types = (typesString == null || typesString.isEmpty() || "NONE".equalsIgnoreCase(typesString)) ? new DamageType[0] : toDamageTypeArray(typesString);
    }

    private DamageType[] toDamageTypeArray(String typesString) {
        String[] split = typesString.split("\\,");
        DamageType[] array = new DamageType[split.length];

        for (int i = 0; i < array.length; i++)
            array[i] = DamageType.valueOf(UtilityMethods.enumName(split[i]));

        return array;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {

        if (target.isDead() || !(target.getBukkitEntity() instanceof LivingEntity) || data.getCaster().isUsingDamageSkill() || target.getHealth() <= 0)
            return SkillResult.INVALID_TARGET;

        // Calculate damage and find element if existing
        double damage = amount.get(data, target) * data.getPower();
        final @Nullable Element element = elementName != null ? Objects.requireNonNull(MythicLib.plugin.getElements().get(elementName), "Could not find element with ID '" + elementName + "'") : null;

        /*
         * If the caster is not a player, an AttackMetadata
         * is now called but with no attacker provided.
         */
        final DamageMetadata dmg = element == null ? new DamageMetadata(damage, types) : new DamageMetadata(damage, element, types);
        applyElementalModifiers((LivingEntity) target.getBukkitEntity(), dmg);
        registerAttack(new AttackMetadata(dmg, (LivingEntity) target.getBukkitEntity(), null));
        return SkillResult.SUCCESS;
    }
    

    private void registerAttack(@NotNull AttackMetadata attack) {
        Validate.notNull(attack.getTarget(), "Target cannot be null"); // BW compatibility check
        attack.getTarget().setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, attack));
        applyDamage(Math.max(attack.getDamage().getDamage(), MINIMUM_DAMAGE), attack.getTarget(), null, true, false);
    }
    
    private void applyDamage(double damage, @NotNull LivingEntity target, @Nullable Player damager, boolean knockback, boolean ignoreImmunity) {

        // Should knockback be applied
        if (!knockback) {
            final AttributeInstance instance = target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            try {
                instance.addModifier(NO_KNOCKBACK);
                applyDamage(damage, target, damager, true, ignoreImmunity);
            } catch (Exception anyError) {
                MythicLib.plugin.getLogger().log(Level.SEVERE, "An error occured while registering player damage");
                anyError.printStackTrace();
            } finally {
                instance.removeModifier(NO_KNOCKBACK);
            }

            // Should damage immunity be taken into account
        } else if (ignoreImmunity) {
            final int noDamageTicks = target.getNoDamageTicks();
            try {
                target.setNoDamageTicks(0);
                applyDamage(damage, target, damager, true, false);
            } catch (Exception anyError) {
                MythicLib.plugin.getLogger().log(Level.SEVERE, "An error occured while registering player damage");
                anyError.printStackTrace();
            } finally {
                target.setNoDamageTicks(noDamageTicks);
            }

            // Calculate the damage that should be done based on elemental defense
        } else {
            if (damager == null)
                target.damage(damage);
            else
                target.damage(damage, damager);
        }
    }

    public void applyElementalModifiers(LivingEntity target, DamageMetadata data) {
        final StatProvider opponentStats = StatProvider.get(target);
        for (DamagePacket p : data.getPackets()) {
        	if (p instanceof ElementalDamagePacket) {
        		ElementalDamagePacket ep = (ElementalDamagePacket) p;
            	Element element = ep.getElement();
            	double damage = ep.getValue();

                // Apply elemental weakness
                final double weakness = opponentStats.getStat(element.getId() + "_WEAKNESS");
                damage *= 1 + Math.max(-1, weakness / 100);
                if (damage == 0)
                    continue;

                // Apply elemental defense
                double defense = opponentStats.getStat(element.getId() + "_DEFENSE");
                defense *= 1 + Math.max(-1, opponentStats.getStat(element.getId() + "_DEFENSE_PERCENT") / 100);
                damage = new DefenseFormula().getAppliedDamage(defense, damage);

                // Register the damage packet
                ep.setValue(damage);
        	}
        }
    }


}

