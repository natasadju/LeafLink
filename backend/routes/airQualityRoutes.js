var express = require('express');
var router = express.Router();
var airQualityController = require('../controllers/airQualityController.js');

/*
 * GET
 */
router.get('/', airQualityController.list);

/*
 * GET
 */
router.get('/:id', airQualityController.show);

/*
 * POST
 */
router.post('/', airQualityController.create);

/*
 * PUT
 */
router.put('/:id', airQualityController.update);

/*
 * DELETE
 */
router.delete('/:id', airQualityController.remove);

module.exports = router;
