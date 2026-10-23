# EntityScaleCore
## 📊 難易度倍率の計算式 (Difficulty Multiplier Calculation)

MOBの最終的な難易度倍率は、距離、経過日数、深さ（高度）などのボーナスを合算し、エリア倍率を乗算して算出されます[cite: 4, 6]。

$$\text{Final Multiplier} = (1 + \text{Distance Bonus} + \text{Day Bonus} + \text{Depth Bonus} + \text{Other Modifiers}) \times \text{Area Multiplier}$$

### 各種ボーナスの詳細

| 要素名 (Element) | 計算式 (Formula) | デフォルト設定 (Default Settings) |
| :--- | :--- | :--- |
| **距離ボーナス**<br>(Distance Bonus) | $\max\left(0, \frac{\text{Distance} - \text{Start}}{\text{Blocks Per Level}}\right) \times \text{Bonus Per Level}$<br><br>※ $\text{Distance} = \sqrt{\Delta X^2 + \Delta Y^2 \times Y\_WEIGHT + \Delta Z^2}$ | • Start Distance: `1000.0`<br>• Blocks Per Level: `100.0`<br>• Bonus Per Level: `+0.1`<br>• Max Bonus: `+10.0`<br>• Y_WEIGHT: `0.25` |
| **日数ボーナス**<br>(Day Bonus) | $\text{Days} \times \text{Bonus Per Day}$<br><br>※ $\text{Days} = \text{Game Time} \div 24000$ | • Bonus Per Day: `+0.01`<br>• Max Enabled: `false`<br>• Max Bonus: `+10.0` |
| **深さボーナス**<br>(Depth Bonus) | $\max(0, \text{Origin Y} - \text{Current Y}) \times \text{Bonus Per Block}$ | • Bonus Per Block: `+0.01`<br>• Max Bonus: `+2.0` |

---

### 計算例 (Example)

各ボーナスおよびエリア倍率が以下の値である場合：
* **Distance Bonus** = `2.0`
* **Day Bonus** = `0.5`
* **Depth Bonus** = `0.3`
* **Area Multiplier** = `1.5`

$$(1 + 2.0 + 0.5 + 0.3) \times 1.5 = 3.8 \times 1.5 = 5.7\times$$

最終的なMOBの難易度倍率は **$5.7\times$** になります。
