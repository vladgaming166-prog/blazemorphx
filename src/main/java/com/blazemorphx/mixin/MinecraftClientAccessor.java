package com.blazemorphx.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
	@Accessor("session")
	Session blaze$getSession();

	@Mutable
	@Accessor("session")
	void blaze$setSession(Session session);

	@Accessor("userApiService")
	UserApiService blaze$getUserApiService();

	@Mutable
	@Accessor("userApiService")
	void blaze$setUserApiService(UserApiService service);

	@Accessor("userPropertiesFuture")
	CompletableFuture<UserApiService.UserProperties> blaze$getUserPropertiesFuture();

	@Mutable
	@Accessor("userPropertiesFuture")
	void blaze$setUserPropertiesFuture(CompletableFuture<UserApiService.UserProperties> future);

	@Accessor("profileKeys")
	ProfileKeys blaze$getProfileKeys();

	@Mutable
	@Accessor("profileKeys")
	void blaze$setProfileKeys(ProfileKeys keys);

	@Accessor("socialInteractionsManager")
	SocialInteractionsManager blaze$getSocialInteractionsManager();

	@Mutable
	@Accessor("socialInteractionsManager")
	void blaze$setSocialInteractionsManager(SocialInteractionsManager manager);

	@Accessor("abuseReportContext")
	AbuseReportContext blaze$getAbuseReportContext();

	@Mutable
	@Accessor("abuseReportContext")
	void blaze$setAbuseReportContext(AbuseReportContext context);

	@Accessor("gameProfileFuture")
	CompletableFuture<?> blaze$getGameProfileFuture();

	@Mutable
	@Accessor("gameProfileFuture")
	void blaze$setGameProfileFuture(CompletableFuture<?> future);
}
