package com.custommusic.init;

import com.custommusic.CustomMusicMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = 
            DeferredRegister.create(Registries.SOUND_EVENT, CustomMusicMod.MODID);


    //Coba
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_404_NEW_ERA = SOUND_EVENTS.register("music_disc_404_new_era",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_404_new_era")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ALL_NIGHT = SOUND_EVENTS.register("music_disc_all_night",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_all_night")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ARMAGEDDON = SOUND_EVENTS.register("music_disc_armageddon",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_armageddon")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BANG_BANG = SOUND_EVENTS.register("music_disc_bang_bang",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_bang_bang")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BIGGER_BADDER_BETTER = SOUND_EVENTS.register("music_disc_bigger_badder_better",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_bigger_badder_better")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BLAZING_HEARTS = SOUND_EVENTS.register("music_disc_blazing_hearts",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_blazing_hearts")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BLUE_VALENTINE = SOUND_EVENTS.register("music_disc_blue_valentine",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_blue_valentine")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BREATHING_UNDERWATER = SOUND_EVENTS.register("music_disc_breathing_underwater",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_breathing_underwater")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BUTTERFLIES = SOUND_EVENTS.register("music_disc_butterflies",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_butterflies")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CHOOM = SOUND_EVENTS.register("music_disc_choom",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_choom")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CRAZY = SOUND_EVENTS.register("music_disc_crazy",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_crazy")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CRAZY_STUPID_LOVE = SOUND_EVENTS.register("music_disc_crazy_stupid_love",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_crazy_stupid_love")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CUPID = SOUND_EVENTS.register("music_disc_cupid",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_cupid")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DANCING_IN_THE_STARLIT_NIGHT = SOUND_EVENTS.register("music_disc_dancing_in_the_starlit_night",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_dancing_in_the_starlit_night")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DDI_RO_RI = SOUND_EVENTS.register("music_disc_ddi_ro_ri",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_ddi_ro_ri")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DUN_DUN = SOUND_EVENTS.register("music_disc_dun_dun",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_dun_dun")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_EARTHQUAKE = SOUND_EVENTS.register("music_disc_earthquake",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_earthquake")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ECHOES = SOUND_EVENTS.register("music_disc_echoes",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_echoes")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_FEARLESS = SOUND_EVENTS.register("music_disc_fearless",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_fearless")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_FIRE = SOUND_EVENTS.register("music_disc_fire",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_fire")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_FLOWER = SOUND_EVENTS.register("music_disc_flower",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_flower")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_FRACTURES = SOUND_EVENTS.register("music_disc_fractures",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_fractures")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GIRLS = SOUND_EVENTS.register("music_disc_girls",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_girls")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GIRLS_WILL_BE_GIRLS = SOUND_EVENTS.register("music_disc_girls_will_be_girls",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_girls_will_be_girls")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GODS = SOUND_EVENTS.register("music_disc_gods",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_gods")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GODS_MENU = SOUND_EVENTS.register("music_disc_gods_menu",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_gods_menu")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GOOD_GIRL = SOUND_EVENTS.register("music_disc_good_girl",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_good_girl")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GOOD_STUFF = SOUND_EVENTS.register("music_disc_good_stuff",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_good_stuff")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GRAVITY = SOUND_EVENTS.register("music_disc_gravity",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_gravity")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HANDS_UP = SOUND_EVENTS.register("music_disc_hands_up",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_hands_up")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HANDS_UP_CB = SOUND_EVENTS.register("music_disc_hands_up_cb",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_hands_up_cb")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HEAVY_SERENADE = SOUND_EVENTS.register("music_disc_heavy_serenade",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_heavy_serenade")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_IN_MY_HANDS = SOUND_EVENTS.register("music_disc_in_my_hands",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_in_my_hands")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ITS_ME = SOUND_EVENTS.register("music_disc_its_me",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_its_me")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_I_AM = SOUND_EVENTS.register("music_disc_i_am",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_i_am")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_I_DO_ME = SOUND_EVENTS.register("music_disc_i_do_me",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_i_do_me")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_JUMP = SOUND_EVENTS.register("music_disc_jump",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_jump")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_KILLIN_IT = SOUND_EVENTS.register("music_disc_killin_it",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_killin_it")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LEMONADE = SOUND_EVENTS.register("music_disc_lemonade",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_lemonade")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LIKE_JENNIE = SOUND_EVENTS.register("music_disc_like_jennie",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_like_jennie")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LOVE_ATTACK = SOUND_EVENTS.register("music_disc_love_attack",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_love_attack")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MAGNETIC = SOUND_EVENTS.register("music_disc_magnetic",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_magnetic")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MANIAC = SOUND_EVENTS.register("music_disc_maniac",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_maniac")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MANTRA = SOUND_EVENTS.register("music_disc_mantra",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_mantra")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_METRONOME = SOUND_EVENTS.register("music_disc_metronome",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_metronome")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MONEY = SOUND_EVENTS.register("music_disc_money",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_money")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_NEW_NEW = SOUND_EVENTS.register("music_disc_new_new",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_new_new")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_NOT_CUTE_ANYMORE = SOUND_EVENTS.register("music_disc_not_cute_anymore",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_not_cute_anymore")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_OMG = SOUND_EVENTS.register("music_disc_omg",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_omg")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_PARADISE = SOUND_EVENTS.register("music_disc_paradise",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_paradise")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_PERFECT_NIGHT = SOUND_EVENTS.register("music_disc_perfect_night",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_perfect_night")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_PSYCHO = SOUND_EVENTS.register("music_disc_psycho",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_psycho")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_REBEL_HEART = SOUND_EVENTS.register("music_disc_rebel_heart",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_rebel_heart")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_REDRED = SOUND_EVENTS.register("music_disc_redred",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_redred")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ROCKSTAR = SOUND_EVENTS.register("music_disc_rockstar",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_rockstar")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_RUDE = SOUND_EVENTS.register("music_disc_rude",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_rude")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SAWADIKA = SOUND_EVENTS.register("music_disc_sawadika",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_sawadika")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SEE_THAT = SOUND_EVENTS.register("music_disc_see_that",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_see_that")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SHEESH = SOUND_EVENTS.register("music_disc_sheesh",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_sheesh")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SOLO = SOUND_EVENTS.register("music_disc_solo",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_solo")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SPAGHETTI = SOUND_EVENTS.register("music_disc_spaghetti",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_spaghetti")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SUPERNOVA = SOUND_EVENTS.register("music_disc_supernova",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_supernova")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SUPER_SHY = SOUND_EVENTS.register("music_disc_super_shy",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_super_shy")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TIL_WE_DIE = SOUND_EVENTS.register("music_disc_til_we_die",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_til_we_die")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TWENTY = SOUND_EVENTS.register("music_disc_twenty",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_twenty")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_UNIQUE = SOUND_EVENTS.register("music_disc_unique",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_unique")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_UP = SOUND_EVENTS.register("music_disc_up",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_up")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_UP_KARINA_SOLO = SOUND_EVENTS.register("music_disc_up_karina_solo",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_up_karina_solo")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WANNABE = SOUND_EVENTS.register("music_disc_wannabe",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_wannabe")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WAY_BACK_HOME = SOUND_EVENTS.register("music_disc_way_back_home",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_way_back_home")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WE_PRAY = SOUND_EVENTS.register("music_disc_we_pray",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_we_pray")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WHAT_IS_LOVE = SOUND_EVENTS.register("music_disc_what_is_love",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_what_is_love")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WHIPLASH = SOUND_EVENTS.register("music_disc_whiplash",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_whiplash")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WORK = SOUND_EVENTS.register("music_disc_work",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_work")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_XO = SOUND_EVENTS.register("music_disc_xo",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_xo")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ZOOM = SOUND_EVENTS.register("music_disc_zoom",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_zoom")));
    //Evan
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_NINE = SOUND_EVENTS.register("music_disc_nine",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_nine")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BABY_BLUE = SOUND_EVENTS.register("music_disc_baby_blue",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_baby_blue")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DRIP_TOO_HARD = SOUND_EVENTS.register("music_disc_drip_too_hard",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_drip_too_hard")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_EL_DORADO = SOUND_EVENTS.register("music_disc_el_dorado",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_el_dorado")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GREECE = SOUND_EVENTS.register("music_disc_greece",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_greece")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GRENADE = SOUND_EVENTS.register("music_disc_grenade",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_grenade")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HOLD_ON_WERE_GOING_HOME = SOUND_EVENTS.register("music_disc_hold_on_were_going_home",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_hold_on_were_going_home")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HOT = SOUND_EVENTS.register("music_disc_hot",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_hot")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LEAVE_THE_DOOR_OPEN = SOUND_EVENTS.register("music_disc_leave_the_door_open",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_leave_the_door_open")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MEET_ME_HALFWAY = SOUND_EVENTS.register("music_disc_meet_me_halfway",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_meet_me_halfway")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_NEVER_CHANGE = SOUND_EVENTS.register("music_disc_never_change",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_never_change")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ON_YOUR_OWN = SOUND_EVENTS.register("music_disc_on_your_own",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_on_your_own")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_OWA_OWA = SOUND_EVENTS.register("music_disc_owa_owa",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_owa_owa")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_RATHER_BE = SOUND_EVENTS.register("music_disc_rather_be",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_rather_be")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SOMEONE_LIKE_YOU = SOUND_EVENTS.register("music_disc_someone_like_you",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_someone_like_you")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_THE_BEACH = SOUND_EVENTS.register("music_disc_the_beach",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_the_beach")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TALKING_TO_THE_MOON = SOUND_EVENTS.register("music_disc_talking_to_the_moon",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_talking_to_the_moon")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TENNESSEE_WHISKEY = SOUND_EVENTS.register("music_disc_tennessee_whiskey",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_tennessee_whiskey")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TIME_FLIES = SOUND_EVENTS.register("music_disc_time_flies",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_time_flies")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WAIT_FOR_U = SOUND_EVENTS.register("music_disc_wait_for_u",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_wait_for_u")));







    //Megan
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ALL_I_ASK_OF_YOU = SOUND_EVENTS.register("music_disc_all_i_ask_of_you",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_all_i_ask_of_you")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_AMAZED = SOUND_EVENTS.register("music_disc_amazed",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_amazed")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BEGIN_AGAIN = SOUND_EVENTS.register("music_disc_begin_again",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_begin_again")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CANT_HELP_FALLING_IN_LOVE = SOUND_EVENTS.register("music_disc_cant_help_falling_in_love",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_cant_help_falling_in_love")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CRYSTAL_SNOW = SOUND_EVENTS.register("music_disc_crystal_snow",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_crystal_snow")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_FALL_FOR_YOU = SOUND_EVENTS.register("music_disc_fall_for_you",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_fall_for_you")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_I_CAN_LOVE_YOU_LIKE_THAT = SOUND_EVENTS.register("music_disc_i_can_love_you_like_that",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_i_can_love_you_like_that")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_I_DO_CHERISH_YOU = SOUND_EVENTS.register("music_disc_i_do_cherish_you",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_i_do_cherish_you")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_I_HAVE_NOTHING = SOUND_EVENTS.register("music_disc_i_have_nothing",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_i_have_nothing")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_I_THINK_THEY_CALL_THIS_LOVE = SOUND_EVENTS.register("music_disc_i_think_they_call_this_love",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_i_think_they_call_this_love")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_I_WONT_GIVE_UP = SOUND_EVENTS.register("music_disc_i_wont_give_up",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_i_wont_give_up")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ILL_BE = SOUND_EVENTS.register("music_disc_ill_be",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_ill_be")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_KEEP_HOLDING_ON = SOUND_EVENTS.register("music_disc_keep_holding_on",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_keep_holding_on")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LUCKY = SOUND_EVENTS.register("music_disc_lucky",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_lucky")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SERENDIPITY = SOUND_EVENTS.register("music_disc_serendipity",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_serendipity")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SMILE = SOUND_EVENTS.register("music_disc_smile",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_smile")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_THANK_GOD = SOUND_EVENTS.register("music_disc_thank_god",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_thank_god")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_THE_REASON = SOUND_EVENTS.register("music_disc_the_reason",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_the_reason")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WAIT_FOR_YOU = SOUND_EVENTS.register("music_disc_wait_for_you",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_wait_for_you")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WANTED = SOUND_EVENTS.register("music_disc_wanted",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_wanted")));





            


    //Adam
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ALL_AROUND_COWBOY = SOUND_EVENTS.register("music_disc_all_around_cowboy",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_all_around_cowboy")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_AMERICA = SOUND_EVENTS.register("music_disc_america",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_america")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DRUM_SHOW = SOUND_EVENTS.register("music_disc_drum_show",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_drum_show")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GARBAGE = SOUND_EVENTS.register("music_disc_garbage",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_garbage")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GETTIN_TIRED_AGAIN = SOUND_EVENTS.register("music_disc_gettin_tired_again",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_gettin_tired_again")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_IM_JUST_A_CLOWN = SOUND_EVENTS.register("music_disc_im_just_a_clown",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_im_just_a_clown")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_KID_AGAIN = SOUND_EVENTS.register("music_disc_kid_again",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_kid_again")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LEAD_THE_WAY = SOUND_EVENTS.register("music_disc_lead_the_way",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_lead_the_way")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MODERN_TIMES = SOUND_EVENTS.register("music_disc_modern_times",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_modern_times")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_OLDIES_STATION = SOUND_EVENTS.register("music_disc_oldies_station",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_oldies_station")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ONE_WAY = SOUND_EVENTS.register("music_disc_one_way",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_one_way")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ROBOT_VOICES = SOUND_EVENTS.register("music_disc_robot_voices",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_robot_voices")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ROUTINES_IN_THE_NIGHT = SOUND_EVENTS.register("music_disc_routines_in_the_night",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_routines_in_the_night")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SWEET_DISPOSITION = SOUND_EVENTS.register("music_disc_sweet_disposition",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_sweet_disposition")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TALLY = SOUND_EVENTS.register("music_disc_tally",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_tally")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TRAVELIN_BLUES = SOUND_EVENTS.register("music_disc_travelin_blues",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_travelin_blues")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_VIGNETTE = SOUND_EVENTS.register("music_disc_vignette",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_vignette")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WHAT_YOU_KNOW = SOUND_EVENTS.register("music_disc_what_you_know",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_what_you_know")));

  


    //Karis
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BABY = SOUND_EVENTS.register("music_disc_baby",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_baby")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BEAUTY_AND_A_BEAT = SOUND_EVENTS.register("music_disc_beauty_and_a_beat",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_beauty_and_a_beat")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ENEMIES = SOUND_EVENTS.register("music_disc_enemies",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_enemies")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HERO = SOUND_EVENTS.register("music_disc_hero",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_hero")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_IT_WILL_RAIN = SOUND_EVENTS.register("music_disc_it_will_rain",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_it_will_rain")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LOOK_AT_THE_SKY = SOUND_EVENTS.register("music_disc_look_at_the_sky",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_look_at_the_sky")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LOVE = SOUND_EVENTS.register("music_disc_love",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_love")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_LOVE_IS_GONE = SOUND_EVENTS.register("music_disc_love_is_gone",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_love_is_gone")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MALIBU_NIGHTS = SOUND_EVENTS.register("music_disc_malibu_nights",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_malibu_nights")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_MOMMAE = SOUND_EVENTS.register("music_disc_mommae",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_mommae")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_OASIS = SOUND_EVENTS.register("music_disc_oasis",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_oasis")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SUMMER = SOUND_EVENTS.register("music_disc_summer",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_summer")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TAKE_YOU_THERE = SOUND_EVENTS.register("music_disc_take_you_there",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_take_you_there")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_THAT_SHOULD_BE_ME = SOUND_EVENTS.register("music_disc_that_should_be_me",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_that_should_be_me")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_THE_GIRLS = SOUND_EVENTS.register("music_disc_the_girls",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_the_girls")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_THREE_FIFTEEN = SOUND_EVENTS.register("music_disc_three_fifteen",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_three_fifteen")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_VERSACE_ON_THE_FLOOR = SOUND_EVENTS.register("music_disc_versace_on_the_floor",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_versace_on_the_floor")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WICKED = SOUND_EVENTS.register("music_disc_wicked",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_wicked")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_YACHT = SOUND_EVENTS.register("music_disc_yacht",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_yacht")));









            

    //Cynthia
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_ARE_YOU_BORED_YET = SOUND_EVENTS.register("music_disc_are_you_bored_yet",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_are_you_bored_yet")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_BAD_DREAM = SOUND_EVENTS.register("music_disc_bad_dream",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_bad_dream")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CALLING_AFTER_ME = SOUND_EVENTS.register("music_disc_calling_after_me",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_calling_after_me")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CALL_YOUR_MOM = SOUND_EVENTS.register("music_disc_call_your_mom",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_call_your_mom")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_COMO_LA_FLOR = SOUND_EVENTS.register("music_disc_como_la_flor",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_como_la_flor")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DROP_DEAD = SOUND_EVENTS.register("music_disc_drop_dead",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_drop_dead")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_EVENING_GLOW = SOUND_EVENTS.register("music_disc_evening_glow",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_evening_glow")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_GOLDEN = SOUND_EVENTS.register("music_disc_golden",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_golden")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_NO_ONE_PARTY_ANTHEM = SOUND_EVENTS.register("music_disc_no_one_party_anthem",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_no_one_party_anthem")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_REMEMBER_WHEN = SOUND_EVENTS.register("music_disc_remember_when",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_remember_when")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WAIT_AND_SEE = SOUND_EVENTS.register("music_disc_wait_and_see",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_wait_and_see")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_YOU = SOUND_EVENTS.register("music_disc_you",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_you")));
}
