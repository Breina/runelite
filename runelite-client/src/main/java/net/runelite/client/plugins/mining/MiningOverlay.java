/*
 * Copyright (c) 2020, Jordan Zomerlei <https://github.com/JZomerlei>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.mining;

import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Instant;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.gameval.AnimationID;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class MiningOverlay extends OverlayPanel
{
	private static final String MINING_RESET = "Reset";
	private static final Set<Integer> WAll_ANIMATIONS = ImmutableSet.of(
		AnimationID.HUMAN_MINING_3A_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_ADAMANT_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_BLACK_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_BRONZE_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_CRYSTAL_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_DRAGON_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_ZALCANO_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_TRAILBLAZER_PICKAXE_NO_INFERNAL_WALL,
		AnimationID.HUMAN_MINING_DRAGON_PICKAXE_PRETTY_WALL,
		AnimationID.HUMAN_MINING_GILDED_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_INFERNAL_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_TRAILBLAZER_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_TRAILBLAZER_RELOADED_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_IRON_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_MITHRIL_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_RUNE_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_STEEL_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_LEAGUE_TRAILBLAZER_PICKAXE_WALL,
		AnimationID.HUMAN_MINING_TRAILBLAZER_RELOADED_PICKAXE_NO_INFERNAL_WALL);

	private final Client client;
	private final MiningPlugin plugin;
	private final MiningConfig config;
	private final XpTrackerService xpTrackerService;

	@Inject
	private MiningOverlay(final Client client, final MiningPlugin plugin, final MiningConfig config, XpTrackerService xpTrackerService)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.xpTrackerService = xpTrackerService;
		addMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Mining overlay");
		addMenuEntry(MenuAction.RUNELITE_OVERLAY, MINING_RESET, "Mining overlay", e -> plugin.setSession(null));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		MiningSession session = plugin.getSession();
		if (session == null || session.getLastMined() == null || !config.showMiningStats())
		{
			return null;
		}

		Pickaxe pickaxe = plugin.getPickaxe();
		if (pickaxe != null &&
				(pickaxe.matchesMiningAnimation(client.getLocalPlayer())
						|| client.getLocalPlayer().getAnimation() == AnimationID.ARCEUUS_CHISEL_ESSENCE
						// when receiving ore from a wall the animation sets to -1 before starting up again
						|| (WAll_ANIMATIONS.contains(plugin.getLastActionAnimationId())
								&& plugin.getLastAnimationChange().isAfter(Instant.now().minusMillis(1800))))
			)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Mining")
				.color(Color.GREEN)
				.build());
		}
		else
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("NOT mining")
				.color(Color.RED)
				.build());
		}

		int actions = xpTrackerService.getActions(Skill.MINING);
		if (actions > 0)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Total mined:")
				.right(Integer.toString(actions))
				.build());

			if (actions > 2)
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left("Mined/hr:")
					.right(Integer.toString(xpTrackerService.getActionsHr(Skill.MINING)))
					.build());
			}
		}

		return super.render(graphics);
	}
}
