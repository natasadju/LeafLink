var express = require('express');
var router = express.Router();
var friendshipController = require('../controllers/friendshipController.js');

/*
 * GET
 */
router.get('/', friendshipController.list);

/*
 * GET
 */
router.get('/:id', friendshipController.show);

/*
 * POST
 */
router.post('/', friendshipController.create);

/*
 * PUT
 */
router.put('/:id', friendshipController.update);

/*
 * DELETE
 */
router.delete('/:id', friendshipController.remove);

module.exports = router;
