package com.custommusic.init;

import com.custommusic.CustomMusicMod;
import com.custommusic.item.CustomMusicDiscItem;
import com.custommusic.item.JukeboxAlbumBlockItem;
import com.custommusic.item.PortableJukeboxAlbumItem;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CustomMusicMod.MODID);

    // Helper: applies disc properties to an existing Properties (with setId already applied)
    private static Item.Properties discProps(Item.Properties props, ResourceKey<JukeboxSong> songKey) {
        return props
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .delayedComponent(DataComponents.JUKEBOX_PLAYABLE, context -> new JukeboxPlayable(context.getOrThrow(songKey)))
                .component(DataComponents.TOOLTIP_DISPLAY,
                    net.minecraft.world.item.component.TooltipDisplay.DEFAULT
                        .withHidden(DataComponents.JUKEBOX_PLAYABLE, true));
    }

    // Helper for registering custom music discs (handles setId via registerItem)
    private static DeferredItem<CustomMusicDiscItem> disc(String name, int comparator, Holder<SoundEvent> sound,
                                                          int lengthTicks, ResourceKey<JukeboxSong> key) {
        return ITEMS.registerItem(name,
                props -> new CustomMusicDiscItem(comparator, sound, discProps(props, key), lengthTicks, key));
    }

    public static final DeferredItem<JukeboxAlbumBlockItem> JUKEBOX_ALBUM = ITEMS.registerItem("jukebox_album",
            props -> new JukeboxAlbumBlockItem(ModBlocks.JUKEBOX_ALBUM.get(), props));

    public static final DeferredItem<PortableJukeboxAlbumItem> PORTABLE_JUKEBOX_ALBUM = ITEMS.registerItem("portable_jukebox_album",
            props -> new PortableJukeboxAlbumItem(props.stacksTo(64)));

    //Coba
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_404_NEW_ERA = disc("music_disc_404_new_era", 29, ModSounds.MUSIC_DISC_404_NEW_ERA, 3591, ModJukeboxSongs.NEW_ERA_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ALL_NIGHT = disc("music_disc_all_night", 118, ModSounds.MUSIC_DISC_ALL_NIGHT, 3955, ModJukeboxSongs.ALL_NIGHT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ARMAGEDDON = disc("music_disc_armageddon", 30, ModSounds.MUSIC_DISC_ARMAGEDDON, 3957, ModJukeboxSongs.ARMAGEDDON_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BANG_BANG = disc("music_disc_bang_bang", 31, ModSounds.MUSIC_DISC_BANG_BANG, 3563, ModJukeboxSongs.BANG_BANG_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BIGGER_BADDER_BETTER = disc("music_disc_bigger_badder_better", 64, ModSounds.MUSIC_DISC_BIGGER_BADDER_BETTER, 4323, ModJukeboxSongs.BIGGER_BADDER_BETTER_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BLAZING_HEARTS = disc("music_disc_blazing_hearts", 1, ModSounds.MUSIC_DISC_BLAZING_HEARTS, 3559, ModJukeboxSongs.BLAZING_HEARTS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BLUE_VALENTINE = disc("music_disc_blue_valentine", 32, ModSounds.MUSIC_DISC_BLUE_VALENTINE, 3720, ModJukeboxSongs.BLUE_VALENTINE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BREATHING_UNDERWATER = disc("music_disc_breathing_underwater", 2, ModSounds.MUSIC_DISC_BREATHING_UNDERWATER, 3559, ModJukeboxSongs.BREATHING_UNDERWATER_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BUTTERFLIES = disc("music_disc_butterflies", 119, ModSounds.MUSIC_DISC_BUTTERFLIES, 3290, ModJukeboxSongs.BUTTERFLIES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CHOOM = disc("music_disc_choom", 94, ModSounds.MUSIC_DISC_CHOOM, 3520, ModJukeboxSongs.CHOOM_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CRAZY = disc("music_disc_crazy", 33, ModSounds.MUSIC_DISC_CRAZY, 3292, ModJukeboxSongs.CRAZY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CRAZY_STUPID_LOVE = disc("music_disc_crazy_stupid_love", 34, ModSounds.MUSIC_DISC_CRAZY_STUPID_LOVE, 3392, ModJukeboxSongs.CRAZY_STUPID_LOVE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CUPID = disc("music_disc_cupid", 65, ModSounds.MUSIC_DISC_CUPID, 3485, ModJukeboxSongs.CUPID_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_DANCING_IN_THE_STARLIT_NIGHT = disc("music_disc_dancing_in_the_starlit_night", 15, ModSounds.MUSIC_DISC_DANCING_IN_THE_STARLIT_NIGHT, 3682, ModJukeboxSongs.DANCING_IN_THE_STARLIT_NIGHT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_DDI_RO_RI = disc("music_disc_ddi_ro_ri", 95, ModSounds.MUSIC_DISC_DDI_RO_RI, 3920, ModJukeboxSongs.DDI_RO_RI_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_DUN_DUN = disc("music_disc_dun_dun", 35, ModSounds.MUSIC_DISC_DUN_DUN, 3848, ModJukeboxSongs.DUN_DUN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_EARTHQUAKE = disc("music_disc_earthquake", 36, ModSounds.MUSIC_DISC_EARTHQUAKE, 3842, ModJukeboxSongs.EARTHQUAKE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ECHOES = disc("music_disc_echoes", 3, ModSounds.MUSIC_DISC_ECHOES, 3916, ModJukeboxSongs.ECHOES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_FEARLESS = disc("music_disc_fearless", 37, ModSounds.MUSIC_DISC_FEARLESS, 3369, ModJukeboxSongs.FEARLESS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_FIRE = disc("music_disc_fire", 117, ModSounds.MUSIC_DISC_FIRE, 2473, ModJukeboxSongs.FIRE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_FLOWER = disc("music_disc_flower", 38, ModSounds.MUSIC_DISC_FLOWER, 3600, ModJukeboxSongs.FLOWER_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_FRACTURES = disc("music_disc_fractures", 4, ModSounds.MUSIC_DISC_FRACTURES, 4905, ModJukeboxSongs.FRACTURES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GIRLS = disc("music_disc_girls", 15, ModSounds.MUSIC_DISC_GIRLS, 3381, ModJukeboxSongs.GIRLS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GIRLS_WILL_BE_GIRLS = disc("music_disc_girls_will_be_girls", 63, ModSounds.MUSIC_DISC_GIRLS_WILL_BE_GIRLS, 3360, ModJukeboxSongs.GIRLS_WILL_BE_GIRLS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GODS = disc("music_disc_gods", 39, ModSounds.MUSIC_DISC_GODS, 4417, ModJukeboxSongs.GODS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GODS_MENU = disc("music_disc_gods_menu", 40, ModSounds.MUSIC_DISC_GODS_MENU, 3358, ModJukeboxSongs.GODS_MENU_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GOOD_GIRL = disc("music_disc_good_girl", 120, ModSounds.MUSIC_DISC_GOOD_GIRL, 2568, ModJukeboxSongs.GOOD_GIRL_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GOOD_STUFF = disc("music_disc_good_stuff", 66, ModSounds.MUSIC_DISC_GOOD_STUFF, 3500, ModJukeboxSongs.GOOD_STUFF_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GRAVITY = disc("music_disc_gravity", 67, ModSounds.MUSIC_DISC_GRAVITY, 3543, ModJukeboxSongs.GRAVITY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_HANDS_UP = disc("music_disc_hands_up", 68, ModSounds.MUSIC_DISC_HANDS_UP, 3850, ModJukeboxSongs.HANDS_UP_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_HANDS_UP_CB = disc("music_disc_hands_up_cb", 99, ModSounds.MUSIC_DISC_HANDS_UP_CB, 4241, ModJukeboxSongs.HANDS_UP_CB_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_HEAVY_SERENADE = disc("music_disc_heavy_serenade", 121, ModSounds.MUSIC_DISC_HEAVY_SERENADE, 3601, ModJukeboxSongs.HEAVY_SERENADE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_IN_MY_HANDS = disc("music_disc_in_my_hands", 96, ModSounds.MUSIC_DISC_IN_MY_HANDS, 2955, ModJukeboxSongs.IN_MY_HANDS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ITS_ME = disc("music_disc_its_me", 97, ModSounds.MUSIC_DISC_ITS_ME, 2766, ModJukeboxSongs.ITS_ME_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_I_AM = disc("music_disc_i_am", 41, ModSounds.MUSIC_DISC_I_AM, 3678, ModJukeboxSongs.I_AM_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_I_DO_ME = disc("music_disc_i_do_me", 42, ModSounds.MUSIC_DISC_I_DO_ME, 3811, ModJukeboxSongs.I_DO_ME_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_JUMP = disc("music_disc_jump", 43, ModSounds.MUSIC_DISC_JUMP, 3294, ModJukeboxSongs.JUMP_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_KILLIN_IT = disc("music_disc_killin_it", 15, ModSounds.MUSIC_DISC_KILLIN_IT, 3523, ModJukeboxSongs.KILLIN_IT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LEMONADE = disc("music_disc_lemonade", 122, ModSounds.MUSIC_DISC_LEMONADE, 3740, ModJukeboxSongs.LEMONADE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LIKE_JENNIE = disc("music_disc_like_jennie", 44, ModSounds.MUSIC_DISC_LIKE_JENNIE, 2472, ModJukeboxSongs.LIKE_JENNIE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LOVE_ATTACK = disc("music_disc_love_attack", 123, ModSounds.MUSIC_DISC_LOVE_ATTACK, 3940, ModJukeboxSongs.LOVE_ATTACK_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MAGNETIC = disc("music_disc_magnetic", 45, ModSounds.MUSIC_DISC_MAGNETIC, 3213, ModJukeboxSongs.MAGNETIC_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MANIAC = disc("music_disc_maniac", 46, ModSounds.MUSIC_DISC_MANIAC, 3655, ModJukeboxSongs.MANIAC_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MANTRA = disc("music_disc_mantra", 47, ModSounds.MUSIC_DISC_MANTRA, 2744, ModJukeboxSongs.MANTRA_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_METRONOME = disc("music_disc_metronome", 98, ModSounds.MUSIC_DISC_METRONOME, 3655, ModJukeboxSongs.METRONOME_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MONEY = disc("music_disc_money", 48, ModSounds.MUSIC_DISC_MONEY, 3354, ModJukeboxSongs.MONEY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_NEW_NEW = disc("music_disc_new_new", 69, ModSounds.MUSIC_DISC_NEW_NEW, 3631, ModJukeboxSongs.NEW_NEW_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_NOT_CUTE_ANYMORE = disc("music_disc_not_cute_anymore", 49, ModSounds.MUSIC_DISC_NOT_CUTE_ANYMORE, 2641, ModJukeboxSongs.NOT_CUTE_ANYMORE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_OMG = disc("music_disc_omg", 50, ModSounds.MUSIC_DISC_OMG, 4246, ModJukeboxSongs.OMG_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_PARADISE = disc("music_disc_paradise", 5, ModSounds.MUSIC_DISC_PARADISE, 4904, ModJukeboxSongs.PARADISE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_PERFECT_NIGHT = disc("music_disc_perfect_night", 51, ModSounds.MUSIC_DISC_PERFECT_NIGHT, 3182, ModJukeboxSongs.PERFECT_NIGHT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_PSYCHO = disc("music_disc_psycho", 52, ModSounds.MUSIC_DISC_PSYCHO, 3909, ModJukeboxSongs.PSYCHO_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_REBEL_HEART = disc("music_disc_rebel_heart", 53, ModSounds.MUSIC_DISC_REBEL_HEART, 3766, ModJukeboxSongs.REBEL_HEART_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_REDRED = disc("music_disc_redred", 15, ModSounds.MUSIC_DISC_REDRED, 3262, ModJukeboxSongs.REDRED_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ROCKSTAR = disc("music_disc_rockstar", 54, ModSounds.MUSIC_DISC_ROCKSTAR, 2764, ModJukeboxSongs.ROCKSTAR_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_RUDE = disc("music_disc_rude", 70, ModSounds.MUSIC_DISC_RUDE, 4000, ModJukeboxSongs.RUDE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SAWADIKA = disc("music_disc_sawadika", 15, ModSounds.MUSIC_DISC_SAWADIKA, 3673, ModJukeboxSongs.SAWADIKA_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SEE_THAT = disc("music_disc_see_that", 55, ModSounds.MUSIC_DISC_SEE_THAT, 3634, ModJukeboxSongs.SEE_THAT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SHEESH = disc("music_disc_sheesh", 56, ModSounds.MUSIC_DISC_SHEESH, 3407, ModJukeboxSongs.SHEESH_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SOLO = disc("music_disc_solo", 57, ModSounds.MUSIC_DISC_SOLO, 3501, ModJukeboxSongs.SOLO_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SPAGHETTI = disc("music_disc_spaghetti", 58, ModSounds.MUSIC_DISC_SPAGHETTI, 3449, ModJukeboxSongs.SPAGHETTI_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SUPERNOVA = disc("music_disc_supernova", 60, ModSounds.MUSIC_DISC_SUPERNOVA, 3582, ModJukeboxSongs.SUPERNOVA_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SUPER_SHY = disc("music_disc_super_shy", 59, ModSounds.MUSIC_DISC_SUPER_SHY, 3093, ModJukeboxSongs.SUPER_SHY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TIL_WE_DIE = disc("music_disc_til_we_die", 15, ModSounds.MUSIC_DISC_TIL_WE_DIE, 4033, ModJukeboxSongs.TIL_WE_DIE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TWENTY = disc("music_disc_twenty", 28, ModSounds.MUSIC_DISC_TWENTY, 3489, ModJukeboxSongs.TWENTY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_UNIQUE = disc("music_disc_unique", 15, ModSounds.MUSIC_DISC_UNIQUE, 3442, ModJukeboxSongs.UNIQUE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_UP = disc("music_disc_up", 6, ModSounds.MUSIC_DISC_UP, 4234, ModJukeboxSongs.UP_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_UP_KARINA_SOLO = disc("music_disc_up_karina_solo", 71, ModSounds.MUSIC_DISC_UP_KARINA_SOLO, 3333, ModJukeboxSongs.UP_KARINA_SOLO_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WANNABE = disc("music_disc_wannabe", 61, ModSounds.MUSIC_DISC_WANNABE, 3825, ModJukeboxSongs.WANNABE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WAY_BACK_HOME = disc("music_disc_way_back_home", 72, ModSounds.MUSIC_DISC_WAY_BACK_HOME, 3851, ModJukeboxSongs.WAY_BACK_HOME_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WE_PRAY = disc("music_disc_we_pray", 27, ModSounds.MUSIC_DISC_WE_PRAY, 4950, ModJukeboxSongs.WE_PRAY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WHAT_IS_LOVE = disc("music_disc_what_is_love", 62, ModSounds.MUSIC_DISC_WHAT_IS_LOVE, 4200, ModJukeboxSongs.WHAT_IS_LOVE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WHIPLASH = disc("music_disc_whiplash", 63, ModSounds.MUSIC_DISC_WHIPLASH, 3666, ModJukeboxSongs.WHIPLASH_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WORK = disc("music_disc_work", 15, ModSounds.MUSIC_DISC_WORK, 3741, ModJukeboxSongs.WORK_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_XO = disc("music_disc_xo", 15, ModSounds.MUSIC_DISC_XO, 3768, ModJukeboxSongs.XO_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ZOOM = disc("music_disc_zoom", 73, ModSounds.MUSIC_DISC_ZOOM, 3365, ModJukeboxSongs.ZOOM_KEY);
    //Evan
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_NINE = disc("music_disc_nine", 7, ModSounds.MUSIC_DISC_NINE, 4234, ModJukeboxSongs.NINE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BABY_BLUE = disc("music_disc_baby_blue", 8, ModSounds.MUSIC_DISC_BABY_BLUE, 35744, ModJukeboxSongs.BABY_BLUE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_DRIP_TOO_HARD = disc("music_disc_drip_too_hard", 9, ModSounds.MUSIC_DISC_DRIP_TOO_HARD, 3083, ModJukeboxSongs.DRIP_TOO_HARD_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_EL_DORADO = disc("music_disc_el_dorado", 10, ModSounds.MUSIC_DISC_EL_DORADO, 4673, ModJukeboxSongs.EL_DORADO_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GREECE = disc("music_disc_greece", 11, ModSounds.MUSIC_DISC_GREECE, 4381, ModJukeboxSongs.GREECE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GRENADE = disc("music_disc_grenade", 12, ModSounds.MUSIC_DISC_GRENADE, 4545, ModJukeboxSongs.GRENADE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_HOLD_ON_WERE_GOING_HOME = disc("music_disc_hold_on_were_going_home", 13, ModSounds.MUSIC_DISC_HOLD_ON_WERE_GOING_HOME, 4502, ModJukeboxSongs.HOLD_ON_WERE_GOING_HOME_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_HOT = disc("music_disc_hot", 14, ModSounds.MUSIC_DISC_HOT, 3862, ModJukeboxSongs.HOT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LEAVE_THE_DOOR_OPEN = disc("music_disc_leave_the_door_open", 15, ModSounds.MUSIC_DISC_LEAVE_THE_DOOR_OPEN, 5039, ModJukeboxSongs.LEAVE_THE_DOOR_OPEN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MEET_ME_HALFWAY = disc("music_disc_meet_me_halfway", 16, ModSounds.MUSIC_DISC_MEET_ME_HALFWAY, 5695, ModJukeboxSongs.MEET_ME_HALFWAY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_NEVER_CHANGE = disc("music_disc_never_change", 17, ModSounds.MUSIC_DISC_NEVER_CHANGE, 4458, ModJukeboxSongs.NEVER_CHANGE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ON_YOUR_OWN = disc("music_disc_on_your_own", 18, ModSounds.MUSIC_DISC_ON_YOUR_OWN, 2686, ModJukeboxSongs.ON_YOUR_OWN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_OWA_OWA = disc("music_disc_owa_owa", 19, ModSounds.MUSIC_DISC_OWA_OWA, 2649, ModJukeboxSongs.OWA_OWA_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_RATHER_BE = disc("music_disc_rather_be", 20, ModSounds.MUSIC_DISC_RATHER_BE, 4559, ModJukeboxSongs.RATHER_BE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SOMEONE_LIKE_YOU = disc("music_disc_someone_like_you", 21, ModSounds.MUSIC_DISC_SOMEONE_LIKE_YOU, 5783, ModJukeboxSongs.SOMEONE_LIKE_YOU_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_THE_BEACH = disc("music_disc_the_beach", 22, ModSounds.MUSIC_DISC_THE_BEACH, 3580, ModJukeboxSongs.THE_BEACH_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TALKING_TO_THE_MOON = disc("music_disc_talking_to_the_moon", 23, ModSounds.MUSIC_DISC_TALKING_TO_THE_MOON, 3980, ModJukeboxSongs.TALKING_TO_THE_MOON_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TENNESSEE_WHISKEY = disc("music_disc_tennessee_whiskey", 24, ModSounds.MUSIC_DISC_TENNESSEE_WHISKEY, 5863, ModJukeboxSongs.TENNESSEE_WHISKEY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TIME_FLIES = disc("music_disc_time_flies", 25, ModSounds.MUSIC_DISC_TIME_FLIES, 3841, ModJukeboxSongs.TIME_FLIES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WAIT_FOR_U = disc("music_disc_wait_for_u", 116, ModSounds.MUSIC_DISC_WAIT_FOR_U, 3829, ModJukeboxSongs.WAIT_FOR_U_KEY);







    //Megan
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ALL_I_ASK_OF_YOU = disc("music_disc_all_i_ask_of_you", 74, ModSounds.MUSIC_DISC_ALL_I_ASK_OF_YOU, 5848, ModJukeboxSongs.ALL_I_ASK_OF_YOU_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_AMAZED = disc("music_disc_amazed", 75, ModSounds.MUSIC_DISC_AMAZED, 4848, ModJukeboxSongs.AMAZED_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BEGIN_AGAIN = disc("music_disc_begin_again", 76, ModSounds.MUSIC_DISC_BEGIN_AGAIN, 4781, ModJukeboxSongs.BEGIN_AGAIN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CANT_HELP_FALLING_IN_LOVE = disc("music_disc_cant_help_falling_in_love", 77, ModSounds.MUSIC_DISC_CANT_HELP_FALLING_IN_LOVE, 3613, ModJukeboxSongs.CANT_HELP_FALLING_IN_LOVE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CRYSTAL_SNOW = disc("music_disc_crystal_snow", 78, ModSounds.MUSIC_DISC_CRYSTAL_SNOW, 6447, ModJukeboxSongs.CRYSTAL_SNOW_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_FALL_FOR_YOU = disc("music_disc_fall_for_you", 79, ModSounds.MUSIC_DISC_FALL_FOR_YOU, 3826, ModJukeboxSongs.FALL_FOR_YOU_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_I_CAN_LOVE_YOU_LIKE_THAT = disc("music_disc_i_can_love_you_like_that", 80, ModSounds.MUSIC_DISC_I_CAN_LOVE_YOU_LIKE_THAT, 5178, ModJukeboxSongs.I_CAN_LOVE_YOU_LIKE_THAT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_I_DO_CHERISH_YOU = disc("music_disc_i_do_cherish_you", 81, ModSounds.MUSIC_DISC_I_DO_CHERISH_YOU, 3959, ModJukeboxSongs.I_DO_CHERISH_YOU_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_I_HAVE_NOTHING = disc("music_disc_i_have_nothing", 82, ModSounds.MUSIC_DISC_I_HAVE_NOTHING, 5783, ModJukeboxSongs.I_HAVE_NOTHING_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_I_THINK_THEY_CALL_THIS_LOVE = disc("music_disc_i_think_they_call_this_love", 83, ModSounds.MUSIC_DISC_I_THINK_THEY_CALL_THIS_LOVE, 3925, ModJukeboxSongs.I_THINK_THEY_CALL_THIS_LOVE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_I_WONT_GIVE_UP = disc("music_disc_i_wont_give_up", 84, ModSounds.MUSIC_DISC_I_WONT_GIVE_UP, 4841, ModJukeboxSongs.I_WONT_GIVE_UP_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ILL_BE = disc("music_disc_ill_be", 85, ModSounds.MUSIC_DISC_ILL_BE, 5330, ModJukeboxSongs.ILL_BE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_KEEP_HOLDING_ON = disc("music_disc_keep_holding_on", 86, ModSounds.MUSIC_DISC_KEEP_HOLDING_ON, 4859, ModJukeboxSongs.KEEP_HOLDING_ON_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LUCKY = disc("music_disc_lucky", 87, ModSounds.MUSIC_DISC_LUCKY, 3814, ModJukeboxSongs.LUCKY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SERENDIPITY = disc("music_disc_serendipity", 88, ModSounds.MUSIC_DISC_SERENDIPITY, 5534, ModJukeboxSongs.SERENDIPITY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SMILE = disc("music_disc_smile", 89, ModSounds.MUSIC_DISC_SMILE, 4200, ModJukeboxSongs.SMILE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_THANK_GOD = disc("music_disc_thank_god", 90, ModSounds.MUSIC_DISC_THANK_GOD, 3494, ModJukeboxSongs.THANK_GOD_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_THE_REASON = disc("music_disc_the_reason", 91, ModSounds.MUSIC_DISC_THE_REASON, 4669, ModJukeboxSongs.THE_REASON_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WAIT_FOR_YOU = disc("music_disc_wait_for_you", 92, ModSounds.MUSIC_DISC_WAIT_FOR_YOU, 5195, ModJukeboxSongs.WAIT_FOR_YOU_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WANTED = disc("music_disc_wanted", 93, ModSounds.MUSIC_DISC_WANTED, 4556, ModJukeboxSongs.WANTED_KEY);







    //Adam
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ALL_AROUND_COWBOY = disc("music_disc_all_around_cowboy", 99, ModSounds.MUSIC_DISC_ALL_AROUND_COWBOY, 3559, ModJukeboxSongs.ALL_AROUND_COWBOY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_AMERICA = disc("music_disc_america", 100, ModSounds.MUSIC_DISC_AMERICA, 4066, ModJukeboxSongs.AMERICA_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_DRUM_SHOW = disc("music_disc_drum_show", 101, ModSounds.MUSIC_DISC_DRUM_SHOW, 4076, ModJukeboxSongs.DRUM_SHOW_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GARBAGE = disc("music_disc_garbage", 102, ModSounds.MUSIC_DISC_GARBAGE, 3934, ModJukeboxSongs.GARBAGE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GETTIN_TIRED_AGAIN = disc("music_disc_gettin_tired_again", 103, ModSounds.MUSIC_DISC_GETTIN_TIRED_AGAIN, 3410, ModJukeboxSongs.GETTIN_TIRED_AGAIN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_IM_JUST_A_CLOWN = disc("music_disc_im_just_a_clown", 104, ModSounds.MUSIC_DISC_IM_JUST_A_CLOWN, 5079, ModJukeboxSongs.IM_JUST_A_CLOWN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_KID_AGAIN = disc("music_disc_kid_again", 105, ModSounds.MUSIC_DISC_KID_AGAIN, 3992, ModJukeboxSongs.KID_AGAIN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LEAD_THE_WAY = disc("music_disc_lead_the_way", 105, ModSounds.MUSIC_DISC_LEAD_THE_WAY, 4772, ModJukeboxSongs.LEAD_THE_WAY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MODERN_TIMES = disc("music_disc_modern_times", 106, ModSounds.MUSIC_DISC_MODERN_TIMES, 3673, ModJukeboxSongs.MODERN_TIMES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_OLDIES_STATION = disc("music_disc_oldies_station", 107, ModSounds.MUSIC_DISC_OLDIES_STATION, 4561, ModJukeboxSongs.OLDIES_STATION_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ONE_WAY = disc("music_disc_one_way", 108, ModSounds.MUSIC_DISC_ONE_WAY, 3266, ModJukeboxSongs.ONE_WAY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ROBOT_VOICES = disc("music_disc_robot_voices", 109, ModSounds.MUSIC_DISC_ROBOT_VOICES, 4751, ModJukeboxSongs.ROBOT_VOICES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ROUTINES_IN_THE_NIGHT = disc("music_disc_routines_in_the_night", 110, ModSounds.MUSIC_DISC_ROUTINES_IN_THE_NIGHT, 4052, ModJukeboxSongs.ROUTINES_IN_THE_NIGHT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SWEET_DISPOSITION = disc("music_disc_sweet_disposition", 111, ModSounds.MUSIC_DISC_SWEET_DISPOSITION, 4730, ModJukeboxSongs.SWEET_DISPOSITION_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TALLY = disc("music_disc_tally", 74, ModSounds.MUSIC_DISC_TALLY, 112, ModJukeboxSongs.TALLY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TRAVELIN_BLUES = disc("music_disc_travelin_blues", 113, ModSounds.MUSIC_DISC_TRAVELIN_BLUES, 3392, ModJukeboxSongs.TRAVELIN_BLUES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_VIGNETTE = disc("music_disc_vignette", 114, ModSounds.MUSIC_DISC_VIGNETTE, 4043, ModJukeboxSongs.VIGNETTE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WHAT_YOU_KNOW = disc("music_disc_what_you_know", 115, ModSounds.MUSIC_DISC_WHAT_YOU_KNOW, 3829, ModJukeboxSongs.WHAT_YOU_KNOW_KEY);






    //Karis
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BABY = disc("music_disc_baby", 124, ModSounds.MUSIC_DISC_BABY, 4280, ModJukeboxSongs.BABY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BEAUTY_AND_A_BEAT = disc("music_disc_beauty_and_a_beat", 125, ModSounds.MUSIC_DISC_BEAUTY_AND_A_BEAT, 4563, ModJukeboxSongs.BEAUTY_AND_A_BEAT_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ENEMIES = disc("music_disc_enemies", 126, ModSounds.MUSIC_DISC_ENEMIES, 3893, ModJukeboxSongs.ENEMIES_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_HERO = disc("music_disc_hero", 127, ModSounds.MUSIC_DISC_HERO, 4989, ModJukeboxSongs.HERO_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_IT_WILL_RAIN = disc("music_disc_it_will_rain", 128, ModSounds.MUSIC_DISC_IT_WILL_RAIN, 5159, ModJukeboxSongs.IT_WILL_RAIN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LOOK_AT_THE_SKY = disc("music_disc_look_at_the_sky", 129, ModSounds.MUSIC_DISC_LOOK_AT_THE_SKY, 6216, ModJukeboxSongs.LOOK_AT_THE_SKY_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LOVE = disc("music_disc_love", 130, ModSounds.MUSIC_DISC_LOVE, 5149, ModJukeboxSongs.LOVE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_LOVE_IS_GONE = disc("music_disc_love_is_gone", 131, ModSounds.MUSIC_DISC_LOVE_IS_GONE, 3480, ModJukeboxSongs.LOVE_IS_GONE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MALIBU_NIGHTS = disc("music_disc_malibu_nights", 132, ModSounds.MUSIC_DISC_MALIBU_NIGHTS, 5125, ModJukeboxSongs.MALIBU_NIGHTS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_MOMMAE = disc("music_disc_mommae", 133, ModSounds.MUSIC_DISC_MOMMAE, 4045, ModJukeboxSongs.MOMMAE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_OASIS = disc("music_disc_oasis", 134, ModSounds.MUSIC_DISC_OASIS, 3824, ModJukeboxSongs.OASIS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_SUMMER = disc("music_disc_summer", 135, ModSounds.MUSIC_DISC_SUMMER, 3997, ModJukeboxSongs.SUMMER_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_TAKE_YOU_THERE = disc("music_disc_take_you_there", 136, ModSounds.MUSIC_DISC_TAKE_YOU_THERE, 4770, ModJukeboxSongs.TAKE_YOU_THERE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_THAT_SHOULD_BE_ME = disc("music_disc_that_should_be_me", 137, ModSounds.MUSIC_DISC_THAT_SHOULD_BE_ME, 4330, ModJukeboxSongs.THAT_SHOULD_BE_ME_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_THE_GIRLS = disc("music_disc_the_girls", 138, ModSounds.MUSIC_DISC_THE_GIRLS, 3241, ModJukeboxSongs.THE_GIRLS_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_THREE_FIFTEEN = disc("music_disc_three_fifteen", 139, ModSounds.MUSIC_DISC_THREE_FIFTEEN, 3342, ModJukeboxSongs.THREE_FIFTEEN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_VERSACE_ON_THE_FLOOR = disc("music_disc_versace_on_the_floor", 140, ModSounds.MUSIC_DISC_VERSACE_ON_THE_FLOOR, 5225, ModJukeboxSongs.VERSACE_ON_THE_FLOOR_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WICKED = disc("music_disc_wicked", 141, ModSounds.MUSIC_DISC_WICKED, 3463, ModJukeboxSongs.WICKED_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_YACHT = disc("music_disc_yacht", 142, ModSounds.MUSIC_DISC_YACHT, 4926, ModJukeboxSongs.YACHT_KEY);






    

    //Cynthia
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_ARE_YOU_BORED_YET = disc("music_disc_are_you_bored_yet", 15, ModSounds.MUSIC_DISC_ARE_YOU_BORED_YET, 3648, ModJukeboxSongs.ARE_YOU_BORED_YET_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_BAD_DREAM = disc("music_disc_bad_dream", 15, ModSounds.MUSIC_DISC_BAD_DREAM, 4220, ModJukeboxSongs.BAD_DREAM_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CALLING_AFTER_ME = disc("music_disc_calling_after_me", 15, ModSounds.MUSIC_DISC_CALLING_AFTER_ME, 3757, ModJukeboxSongs.CALLING_AFTER_ME_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_CALL_YOUR_MOM = disc("music_disc_call_your_mom", 15, ModSounds.MUSIC_DISC_CALL_YOUR_MOM, 5614, ModJukeboxSongs.CALL_YOUR_MOM_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_COMO_LA_FLOR = disc("music_disc_como_la_flor", 15, ModSounds.MUSIC_DISC_COMO_LA_FLOR, 3602, ModJukeboxSongs.COMO_LA_FLOR_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_DROP_DEAD = disc("music_disc_drop_dead", 15, ModSounds.MUSIC_DISC_DROP_DEAD, 4491, ModJukeboxSongs.DROP_DEAD_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_EVENING_GLOW = disc("music_disc_evening_glow", 15, ModSounds.MUSIC_DISC_EVENING_GLOW, 4328, ModJukeboxSongs.EVENING_GLOW_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_GOLDEN = disc("music_disc_golden", 15, ModSounds.MUSIC_DISC_GOLDEN, 3842, ModJukeboxSongs.GOLDEN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_NO_ONE_PARTY_ANTHEM = disc("music_disc_no_one_party_anthem", 15, ModSounds.MUSIC_DISC_NO_ONE_PARTY_ANTHEM, 4863, ModJukeboxSongs.NO_ONE_PARTY_ANTHEM_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_REMEMBER_WHEN = disc("music_disc_remember_when", 15, ModSounds.MUSIC_DISC_REMEMBER_WHEN, 3106, ModJukeboxSongs.REMEMBER_WHEN_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_WAIT_AND_SEE = disc("music_disc_wait_and_see", 15, ModSounds.MUSIC_DISC_WAIT_AND_SEE, 5468, ModJukeboxSongs.WAIT_AND_SEE_KEY);
    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_YOU = disc("music_disc_you", 15, ModSounds.MUSIC_DISC_YOU, 3344, ModJukeboxSongs.YOU_KEY);
}
