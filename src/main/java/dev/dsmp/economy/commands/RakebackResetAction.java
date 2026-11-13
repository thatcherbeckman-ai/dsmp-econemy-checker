package dev.dsmp.economy.commands;

/** Implemented by the client so the command uses its scope checks and save path. */
public interface RakebackResetAction {
    int resetAllRakeback();
}
