package fr.jestiz.core.fancymessage

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent

class FancyMessage(val msg: String) {

    companion object {
        val SHOW_TEXT = HoverEvent.Action.SHOW_TEXT
        val SHOW_ACHIEVEMENT = HoverEvent.Action.SHOW_ACHIEVEMENT
        val SHOW_ITEM = HoverEvent.Action.SHOW_ITEM
        val SHOW_ENTITY = HoverEvent.Action.SHOW_ENTITY
        val OPEN_URL = ClickEvent.Action.OPEN_URL
        val OPEN_FILE = ClickEvent.Action.OPEN_FILE
        val RUN_COMMAND = ClickEvent.Action.RUN_COMMAND
        val SUGGEST_COMMAND = ClickEvent.Action.SUGGEST_COMMAND
        val CHANGE_PAGE = ClickEvent.Action.CHANGE_PAGE
    }

    private val component = ComponentBuilder(msg)
    lateinit var clickAction: ClickEvent.Action
    lateinit var hoverAction: HoverEvent.Action

    fun clickEvent(event: ClickEvent.Action): FancyMessage {
        clickAction = event
        return this
    }

    fun hoverEvent(event: HoverEvent.Action): FancyMessage {
        hoverAction = event
        return this
    }

    fun hover(msg: String): FancyMessage {
        component.event(HoverEvent(hoverAction, ComponentBuilder(msg).create()))
        return this
    }

    fun click(msg: String): FancyMessage {
        component.event(ClickEvent(clickAction, msg))
        return this
    }

    fun build() = component.create()

}