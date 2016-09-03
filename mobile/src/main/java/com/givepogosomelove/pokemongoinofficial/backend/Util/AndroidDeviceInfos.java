package com.givepogosomelove.pokemongoinofficial.backend.Util;

import android.os.Build;

import com.pokegoapi.api.device.DeviceInfos;


/**
 * Created by Angelo on 28.08.2016.
 */
public class AndroidDeviceInfos implements DeviceInfos {
	/**
	 * adb.exe shell getprop ro.product.board
	 *
	 * @return android board name, for example: "angler"
	 */
	@Override
	public String getAndroidBoardName() {
		return Build.BOARD;
	}

	/**
	 * adb.exe shell getprop ro.boot.bootloader
	 *
	 * @return android bootloader, for example: "angler-03.58"
	 */
	@Override
	public String getAndroidBootloader() {
		return Build.BOOTLOADER;
	}

	/**
	 * adb.exe shell getprop ro.product.brand
	 *
	 * @return device brand, for example: "google"
	 */
	@Override
	public String getDeviceBrand() {
		return Build.BRAND;
	}

	/**
	 * adb.exe shell settings get secure android_id
	 * UUID.randomUUID().toString();
	 *
	 * @return device id, for example: "****************"
	 */
	@Override
	public String getDeviceId() {
		return Build.DEVICE;
	}

	/**
	 * adb.exe shell getprop ro.product.model
	 *
	 * @return device model, for example: "Nexus 6P"
	 */
	@Override
	public String getDeviceModel() {
		return Build.MODEL;
	}

	/**
	 * adb.exe shell getprop ro.product.name
	 *
	 * @return device model identifier, for example: "angler"
	 */
	@Override
	public String getDeviceModelIdentifier() {
		return Build.BOARD;
	}

	/**
	 * Always qcom
	 *
	 * @return device boot model, for example: "qcom"
	 */
	@Override
	public String getDeviceModelBoot() {
		return "qcom";
	}

	/**
	 * adb.exe shell getprop ro.product.manufacturer
	 *
	 * @return hardware manufacturer, for example: "Huawei"
	 */
	@Override
	public String getHardwareManufacturer() {
		return Build.MANUFACTURER;
	}

	/**
	 * adb.exe shell getprop ro.product.model
	 *
	 * @return hardware model, for example: "Nexus 6P"
	 */
	@Override
	public String getHardwareModel() {
		return Build.MODEL;
	}

	/**
	 * adb.exe shell getprop ro.product.name
	 *
	 * @return firmware brand, for example: "angler"
	 */
	@Override
	public String getFirmwareBrand() {
		return Build.BOARD;
	}

	/**
	 * adb.exe shell getprop ro.build.tags
	 *
	 * @return firmware tags, for example: "release-keys"
	 */
	@Override
	public String getFirmwareTags() {
		return Build.TAGS;
	}

	/**
	 * adb.exe shell getprop ro.build.type
	 *
	 * @return firmware type, for example: "user"
	 */
	@Override
	public String getFirmwareType() {
		return Build.TYPE;
	}

	/**
	 * adb.exe shell getprop ro.build.fingerprint
	 *
	 * @return firmware fingerprint, for example: "google/angler/angler:7.0/NPD90G/3051502:user/release-keys"
	 */
	@Override
	public String getFirmwareFingerprint() {
		return Build.FINGERPRINT;
	}
}
