package com.mygdx.hadal.input;

import com.mygdx.hadal.managers.GameStateManager.State;
import com.mygdx.hadal.schmucks.MoveStates;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.server.PacketEffect;
import com.mygdx.hadal.states.HubState;
import com.mygdx.hadal.states.PlayState;

/**
 * TODO:.
 * @author Zachary Tu
 *
 */
public class ActionController {

	private Player player;
	private PlayState state;
	
	//Is the player currently holding move left/right? This is used for processing holding both buttons -> releasing one. 
	private boolean leftDown = false;
	private boolean rightDown = false;
	
	public ActionController(Player player, PlayState state) {
		this.player = player;
		this.state = state;
	}
	
	public boolean keyDown(PlayerAction action) {
		
		if (player == null) return true;

		if (player.getPlayerData() == null) return true;

		if (action == PlayerAction.WALK_LEFT) {
			leftDown = true;
			if (!rightDown) {
				player.setMoveState(MoveStates.MOVE_LEFT);
			} else {
				player.setMoveState(MoveStates.STAND);
			}
		}
		
		if (action == PlayerAction.WALK_RIGHT) {
			rightDown = true;
			if (!leftDown) {
				player.setMoveState(MoveStates.MOVE_RIGHT);
			} else {
				player.setMoveState(MoveStates.STAND);
			}
		}
		
		if (action == PlayerAction.JUMP) {
			player.setHovering(true);
			player.jump();
		}
		
		if (action == PlayerAction.CROUCH) {
			player.fastFall();
		}
		
		if (action == PlayerAction.INTERACT) {
			//ATM, event interaction also advances dialog
			if (state.getStage() != null) {
				state.getStage().nextDialogue();
			}
			
			player.interact();
		}
		
		if (action == PlayerAction.FREEZE) {
			player.activeItem();
		}
		
		if (action == PlayerAction.RELOAD) {
			player.reload();
		}
		
		if (action == PlayerAction.FIRE) {
			player.setShooting(true);
		}
		
		if (action == PlayerAction.BOOST) {
			player.airblast();
		}
		
		if (action == PlayerAction.SWITCH_TO_LAST) {
			player.switchToLast();
		}
		
		if (action == PlayerAction.SWITCH_TO_1) {
			player.switchToSlot(1);
		}
		
		if (action == PlayerAction.SWITCH_TO_2) {
			player.switchToSlot(2);
		}
		
		if (action == PlayerAction.SWITCH_TO_3) {
			player.switchToSlot(3);
		}
		
		if (action == PlayerAction.SWITCH_TO_4) {
			player.switchToSlot(4);
		}
		
		if (action == PlayerAction.SWITCH_TO_5) {
			player.switchToSlot(4);
		}
		
		if (action == PlayerAction.DIALOGUE) {
			if (state.getStage() != null) {
				state.getStage().nextDialogue();
			}
		}
		
		if (action == PlayerAction.PAUSE) {
			if (player.equals(state.getPlayer())) {
				state.getGsm().addState(State.MENU, PlayState.class);
				state.getGsm().addState(State.MENU, HubState.class);
			} else {
				state.addPacketEffect(new PacketEffect() {

					@Override
					public void execute() {
						state.getGsm().addState(State.MENU, PlayState.class);
						state.getGsm().addState(State.MENU, HubState.class);
					}
				});
			}
		}
		
		if (action == PlayerAction.MO_CYCLE_UP) {
			player.getPlayerData().switchUp();
		}
		
		if (action == PlayerAction.MO_CYCLE_DOWN) {
			player.getPlayerData().switchDown();
		}
		
		return false;
	}

	public boolean keyUp(PlayerAction action) {
		
		if (player == null) return true;

		if (action == PlayerAction.WALK_LEFT) {
			leftDown = false;
			if (rightDown) {
				player.setMoveState(MoveStates.MOVE_RIGHT);
			} else {
				player.setMoveState(MoveStates.STAND);
			}
		}
		
		if (action == PlayerAction.WALK_RIGHT) {
			rightDown = false;
			if (leftDown) {
				player.setMoveState(MoveStates.MOVE_LEFT);
			} else {
				player.setMoveState(MoveStates.STAND);
			}
		}
		
		if (action == PlayerAction.JUMP) {
			player.setHovering(false);
		}
		
		if (action == PlayerAction.FIRE) {
			player.setShooting(false);
			player.release();
		}
				
		return false;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}
