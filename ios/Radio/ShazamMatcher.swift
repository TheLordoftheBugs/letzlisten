//
//  ShazamMatcher.swift
//  Radio Lëtzebuerg
//
//  ShazamKit integration — identifies the current track when no ICY metadata is available.
//
//  NOTE: Requires the "ShazamKit" capability to be enabled in the Xcode target's
//  Signing & Capabilities tab (adds the com.apple.developer.shazamkit entitlement).
//

import ShazamKit
import AVFoundation

// MARK: - Tap context (passed through MTAudioProcessingTap via clientInfo)

@available(iOS 15.0, *)
final class ShazamTapContext {
    weak var matcher: ShazamMatcher?
    var format: AVAudioFormat?

    init(matcher: ShazamMatcher) {
        self.matcher = matcher
    }
}

// MARK: - ShazamMatcher

@available(iOS 15.0, *)
final class ShazamMatcher: NSObject, SHSessionDelegate {

    private var session: SHSession?

    /// Called on the main thread when a match is found.
    var onMatch: ((String, String) -> Void)?

    private(set) var isRunning = false

    // MARK: Control

    func start() {
        guard !isRunning else { return }
        isRunning = true
        session = SHSession()
        session?.delegate = self
        print("🎵 ShazamKit: démarrage de la reconnaissance")
    }

    func stop() {
        guard isRunning else { return }
        isRunning = false
        session = nil
        print("🎵 ShazamKit: arrêt")
    }

    // MARK: Feed audio

    func match(buffer: AVAudioPCMBuffer) {
        guard isRunning else { return }
        session?.matchStreamingBuffer(buffer, at: nil)
    }

    // MARK: SHSessionDelegate

    func session(_ session: SHSession, didFind match: SHMatch) {
        guard let item = match.mediaItems.first,
              let title = item.title, !title.isEmpty else { return }
        let artist = item.artist ?? ""
        print("🎵 ShazamKit: trouvé « \(title) » par « \(artist) »")
        stop()
        DispatchQueue.main.async { [weak self] in
            self?.onMatch?(title, artist)
        }
    }

    func session(_ session: SHSession, didNotFindMatchFor signature: SHSignature, error: Error?) {
        if let error = error {
            print("🎵 ShazamKit: pas de correspondance — \(error.localizedDescription)")
        }
    }
}
