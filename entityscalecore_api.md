# EntityScaleCore API 詳細仕様書

`EntityScaleCore` は、マインクラフト（Minecraft）環境において、エンティティ（モブ）のスケール（倍率やスケーリング）の計算、管理、および拡張機能を提供するAPIです。外部Modや内部処理から利用される各マネージャー、APIクラス、およびデータ構造の詳細な仕様を解説します。

## 1. パッケージ構成

* `com.omuraisu.entityscalecore.api`: 公開APIのインターフェース、コンテキスト、結果、およびエントリポイント

* `com.omuraisu.entityscalecore.area`: 3次元空間におけるエリアの定義と、ディメンションごとの空間管理

* `com.omuraisu.entityscalecore.core`: 特定のブロック座標に紐づく「コア（Core）」構造およびその特性管理

## 2. API エントリポイントと基本構造

### 2.1. `EntityScaleCoreAPI`

APIインスタンスのシングルトン管理を行うエントリポイントクラスです。

* **メソッド**:

  * `public static void setInstance(ScalingAPI api)`

    * **引数**: `ScalingAPI api` - 設定するAPIの実装インスタンス

    * **処理・挙동**: グローバルなAPIインスタンスを登録します。通常はModの初期化時に内部で呼び出されます。

  * `public static void reset()`

    * **処理・挙動**: 登録されているAPIインスタンスを `null` にリセットします。ワールド終了時などに使用されます。

  * `public static ScalingAPI get()`

    * **戻り値**: `ScalingAPI`

    * **処理・挙動**: 現在有効な `ScalingAPI` インスタンスを返します。インスタンスが未設定（ワールド未ロード等）の場合は `IllegalStateException` をスローします。

## 3. スケーリング・コンテキスト・結果

### 3.1. `MobScaleContext`

モブのスケーリング計算時に参照される、位置や環境などのコンテキスト情報を保持するイミュータブルなデータクラスです。

* **保持フィールド・ゲッター**:

  * `double x()`, `y()`, `z()`: 対象モブのワールド内座標

  * `long gameTime()`: 現在のゲーム内時間（ティック）

  * `ResourceKey<Level> dimension()`: ディメンションのキー（例: `Level.OVERWORLD`）

  * `EntityType<?> entityType()`: モブのエンティティタイプ

  * `double distanceFromOrigin()`: 原点からの直線距離

  * `double weightedDistance()`: 重み付けされた距離

  * `double depth()`: 深度（高さや地下深度に関する指標）

### 3.2. `MobScaleResult`

スケーリングの最終的な計算結果を保持するクラスです。

* **プロパティ・ゲッター**:

  * `double multiplier()`: 最終的なスケール倍率

  * `boolean isSuppressed()`: スケーリングが抑制（無効化・固定化）されているかどうか

  * `boolean isForced()`: スケールが強制適用されているかどうか

  * `ScaleSource source()`: スケール決定の主要因となったソース (`ScaleSource`)

### 3.3. `ScalingBreakdown`

スケール計算の詳細な内訳（各ボーナスや倍率の寄与度）を保持するレコードです。

* **プロパティ**:

  * `double distanceBonus`: 距離によるボーナス倍率

  * `double dayBonus`: 日数経過によるボーナス倍率

  * `double depthBonus`: 深度によるボーナス倍率

  * `double areaMultiplier`: エリアによる倍率

  * `double totalMultiplier`: すべてを統合した合計倍率

## 4. エリア管理システム (`com.omuraisu.entityscalecore.area`)

### 4.1. `Area` (Record)

3次元の直方体領域、倍率、優先度、対象エンティティを保持するレコードです。

* **コンストラクタの補正処理**:

  * 引数として渡された `minX` と `maxX`、`minZ` と `maxZ`、`minY` と `maxY` は、順不同で入力されても自動的に `Math.min` と `Math.max` により大小関係が整えられます。

  * `targetEntities` は安全のため `List.copyOf()` によりイミュータブルなリストに変換されます。

* **主要メソッド**:

  * `boolean contains(double x, double y, double z)`: 指定された浮動小数点座標が領域の境界内（境界線上を含む）に収まっているかを判定します。

  * `boolean targets(ResourceLocation entityType)`: 指定されたエンティティタイプが対象に含まれているかを判定します。対象リストが空の場合は、すべてのエンティティが対象となります。

### 4.2. `AreaHandle` & `AreaHandleImpl`

登録されたエリアを外部から安全に参照・操作するためのハンドルです。

* **メソッド**:

  * `String id()`: エリア固有の識別子文字列を返します。

  * `void remove()`: 該当エリアをマネージャーから削除し、ハンドルを無効化します（2回目以降の呼び出しは無視されます）。

  * `boolean isValid()`: エリアが現在も有効（削除されていない）かどうかを返します。

  * `boolean contains(int x, int y, int z)`: ハンドルが有効な場合に限り、指定座標がエリア内にあるかを判定します。

### 4.3. `AreaManager`

ディメンションごとのエリアを効率的に保持・検索・管理するマネージャークラスです。

* **主要メソッドと内部挙動**:

  * `void registerArea(ResourceKey<Level> dimension, Area area)`

    * **引数**: `dimension` (ディメンションキー), `area` (登録するAreaオブジェクト)

    * **挙動**: 内部の `HashMap<ResourceKey<Level>, List<Area>>` にエリアを追加します。

  * `boolean removeArea(ResourceKey<Level> dimension, String id)`

    * **引数**: `dimension` (ディメンションキー), `id` (削除するエリアのID)

    * **戻り値**: 削除に成功した場合は `true`

    * **挙動**: 指定ディメンションのリストから一致するIDのエリアを削除します。リストが空になった場合、ディメンションのキー自体をマップから除去します。

  * `List<Area> getAreasAt(ResourceKey<Level> dimension, double x, double y, double z, ResourceLocation entityType)`

    * **引数**: `dimension`, `x`, `y`, `z`, `entityType`

    * **挙動**: 指定したディメンション、座標、エンティティに合致するすべてのエリアを抽出し、優先度 (`priority`) の高い順（降順）にソートしたリストを返します。

  * `Area getHighestPriorityArea(...)`

    * **挙動**: `getAreasAt` の結果から、最も優先度の高い先頭のエリアを1つ返します（該当なしの場合は `null`）。

## 5. コア管理システム (`com.omuraisu.entityscalecore.core`)

### 5.1. `CoreType` (Enum)

コアの種類と、それらが持つスケーリング上の特性（抑制・強制）を定義する列挙型です。

* **定数定義**:

  * `NORMAL`: 通常コア（抑制: `false`, 強制: `false`）

  * `SAFE`: セーフコア（抑制: `true`, 強制: `false`）

  * `RAGE`: レイジコア（抑制: `false`, 強制: `true`）

  * `CUSTOM`: カスタムコア（抑制: `false`, 強制: `false`）

  * `CUSTOM_SAFE`: カスタムセーフコア（抑制: `true`, 強制: `false`）

  * `CUSTOM_RAGE`: カスタムレイジコア（抑制: `false`, 強制: `true`）

* **主要メソッド**:

  * `boolean isSuppressed()`: スケール増加や適用を抑制する効果を持つか。

  * `boolean isForced()`: スケールを強制適用する効果を持つか。

  * `static CoreType fromString(String name)`: 文字列から安全に Enum を逆引きします。大文字小文字を問わず、失敗した場合はデフォルトで `NORMAL` を返します。

### 5.2. `CoreManager`

ワールド内に設置される「コア（Core）」ブロックの位置と、それに付随する領域・効果タイプを一元管理するマネージャークラスです。

* **主要メソッドと詳細引数・挙動**:

  * `public void registerCoreArea(...)`

    * **引数**:

      * `ResourceKey<Level> dimension`: コアが存在するディメンション

      * `BlockPos corePosition`: コアブロックの設置座標

      * `String areaId`: 連動させるエリアの固有ID

      * `CoreType coreType`: コアの種類（`SAFE`, `RAGE` など）

      * `int minX, maxX, minZ, maxZ, minY, maxY`: 領域の境界座標（立体範囲の最小・最大）

      * `double multiplier`: このエリアが適用するスケール倍率

      * `int priority`: エリアの優先度

    * **内部処理**:

      1. 内部の `ScalingAPI.registerArea` を呼び出して、指定された立体範囲と倍率を持つエリアをシステムに登録し、`AreaHandle` を取得します。

      2. 取得したハンドルと `corePosition`、および `coreType` を内部のディメンション別マップ（`coreAreas`, `coreTypes`）に紐づけて保持します。

  * `public void registerSafeCore(...)`

    * **引数**: `dimension`, `corePosition`, `areaId`, `minX`, `maxX`, `minZ`, `maxZ`, `minY`, `maxY`, `priority`

    * **内部処理**: 倍率を `1.0`、コアタイプを `CoreType.SAFE` に固定して `registerCoreArea` を呼び出します。

  * `public void registerRageCore(...)`

    * **引数**: `dimension`, `corePosition`, `areaId`, `minX`, `maxX`, `minZ`, `maxZ`, `minY`, `maxY`, `multiplier`, `priority`

    * **内部処理**: コアタイプを `CoreType.RAGE` に固定して `registerCoreArea` を呼び出します。

  * `public boolean removeCoreArea(ResourceKey<Level> dimension, BlockPos corePosition)`

    * **引数**: `dimension` (ディメンション), `corePosition` (コアのブロック座標)

    * **戻り値**: 削除に成功した場合は `true`

    * **内部処理**: 指定座標に紐づく `AreaHandle` を検索し、存在する場合は `handle.remove()` を呼び出して空間システムからエリアを消去するとともに、コアの管理マップからも安全にデータを除去します。

  * `public CoreType getCoreTypeByAreaId(ResourceKey<Level> dimension, String areaId)`

    * **引数**: `dimension` (ディメンション), `areaId` (エリアID)

    * **戻り値**: 該当する `CoreType`（見つからない場合は `null`）

    * **内部処理**: 登録されているコアエリアを走査し、指定された `areaId` と一致するハンドルを持つコアのタイプを逆引きして返します。

## 6. Commonプロジェクト（プラットフォーム共通コード）での開発・利用上の注意点

マルチプラットフォーム対応プロジェクト（Architectury APIやFabric/Forge/NeoForgeの共通モジュール等）において、APIや各マネージャーを利用する際の重要な注意点とトラブルシューティングを記載します。

### 6.1. 依存関係とクラスのスコープ
* **依存関係の反映**: プラットフォーム非依存の共通部分（`common` プロジェクトなど）にコードやテストコードを配置する場合、ビルドスクリプト（`build.gradle`等）において `EntityScaleCore` の依存関係が共通ソースセット（`common`）側に対して正しくルーティング・適用されている必要があります。
* **コンパイルエラーの防止**: プラットフォーム依存側（`fabric`, `neoforge` 等）にのみ依存関係が定義されている状態で共通コード側にAPI利用コードを記述すると、コンパイル時にメソッドやクラスが正しく認識されず、「適用できません」といったシグネチャ不一致や参照エラーの原因になります。

### 6.2. ライフサイクルと初期化タイミング
* **シングルトンのロード状態**: `EntityScaleCoreAPI.get()` はワールドがロードされ、APIの実効インスタンスがセットアップされた後にのみ機能します。
* **初期化順序の考慮**: Modの初期化イベント（CommonInitializerなど）の早期段階でAPIを呼び出すと、まだインスタンスが初期化されておらず `IllegalStateException` が発生する恐れがあります。APIの呼び出しは、ワールドのロード完了後、またはイベントリスナー内で行うようにしてください。